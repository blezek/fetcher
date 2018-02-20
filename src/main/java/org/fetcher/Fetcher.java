package org.fetcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.RateLimiter;

import org.dcm4che3.data.Tag;
import org.fetcher.dicom.CFind;
import org.fetcher.model.Job;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Fetcher {
  static Logger logger = LoggerFactory.getLogger(Fetcher.class);
  Job job;

  enum FetcherState {
    STOPPED, RUNNING
  };

  FetcherState state = FetcherState.STOPPED;

  RateLimiter moveLimit;
  RateLimiter queryLimit;

  ThreadPoolExecutor movePool;
  ThreadPoolExecutor queryPool;

  public Fetcher(Job job) {
    this.job = job;
  }

  @JsonIgnore
  public void update(Job job) {
    throwIfStateIsNot(FetcherState.STOPPED);
    this.job = job;
  }

  /**
   * @throws IllegalArgumentException
   */
  public void throwIfStateIsNot(FetcherState expected) throws IllegalArgumentException {
    if (state != expected) {
      throw new IllegalArgumentException("expected state to be " + expected + " was " + state);
    }
  }

  @JsonProperty
  public Job getJob() {
    return job;
  }

  @JsonProperty("status")
  public JsonNode getPools() {
    ObjectNode node = Main.objectMapper.createObjectNode();
    ObjectNode p = node.putObject("pool");
    if (state == FetcherState.RUNNING) {
      ObjectNode n;
      n = p.putObject("query");
      n.put("active_count", queryPool.getActiveCount());
      n.put("completed_count", queryPool.getCompletedTaskCount());
      n = node.putObject("move");
      n.put("active_count", movePool.getActiveCount());
      n.put("completed_count", movePool.getCompletedTaskCount());
    }
    p = node.putObject("query");
    p.put("count", Main.jdbi.withHandle(handle -> {
      return handle.createQuery("select count(*) from query where job_id = ?").bind(0, job.getJobId()).mapTo(Integer.TYPE).first();
    }));
    // Breakdown by state
    for (State s : State.values()) {
      p.put(s.toString().toLowerCase(), Main.jdbi.withHandle(handle -> {
        return handle.createQuery("select count(*) from query where job_id = ? and status = ?").bind(0, job.getJobId()).bind(1, s.toString()).mapTo(Integer.TYPE).first();
      }));
    }
    return node;
  }

  public void queueAll() {
    throwIfStateIsNot(FetcherState.STOPPED);
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update query set status = :status where job_id = :job_id").bind("status", State.QUEUED.toString()).bind("job_id", job.getJobId()).execute();
    });
  }

  public void queue(int qid) {
    throwIfStateIsNot(FetcherState.STOPPED);
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update query set status = :status where job_id = :job_id and queue_id = :queue_id").bind("status", State.SUCCEEDED.toString()).bind("job_id", job.getJobId()).bind("queue_id", qid).execute();
    });
  }

  @UnitOfWork
  public void start() {

    if (state == FetcherState.RUNNING) {
      return;
    }
    state = FetcherState.RUNNING;

    moveLimit = RateLimiter.create(job.getMovesPerSecond());
    queryLimit = RateLimiter.create(job.getQueriesPerSecond());
    movePool = new ThreadPoolExecutor(job.getConcurrentMoves(), job.getConcurrentMoves(), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    queryPool = new ThreadPoolExecutor(job.getConcurrentQueries(), job.getConcurrentQueries(), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    List<Integer> l = Main.jdbi.withHandle(handle -> {
      return handle.createQuery("select query_id from query where job_id = ? and status = ?").bind(0, job.getJobId()).bind(1, State.QUEUED).mapTo(Integer.TYPE).list();
    });
    job = Main.jobDAO.update(job);
    // Push them all into the queue
    for (Query query : job.getQueries()) {
      queryPool.execute(() -> {
        // Execute the DICOM query
        logger.info("query for " + query);
        Main.jdbi.useHandle(handle -> {
          handle.update("delete from move where query_id = ?", query.getId());
        });
        CFind find = new CFind(job, query);
        try {
          find.execute(data -> {
            logger.info("Got data: " + data);
            Main.jdbi.useHandle(handle -> {
              handle.update("insert into move (study_instance_uid, series_instance_uid, status, message ) values ( ?, ?, ?, ? )", data.getString(Tag.StudyInstanceUID, null), data.getString(Tag.SeriesInstanceUID, null), State.CREATED.toString(), null);
            });
          });
        } catch (Exception e) {
          query.setStatus(State.FAILED);
          query.setMessage(e.getMessage());
          Main.queueDAO.update(query);
        }
      });
    }
  }

  /**
   * Stop processing and wait for all pools to stop
   * 
   * @throws InterruptedException
   */
  public void stop() throws InterruptedException {
    if (state == FetcherState.STOPPED) {
      return;
    }

    state = FetcherState.STOPPED;

    movePool.shutdownNow();
    queryPool.shutdownNow();
    movePool.awaitTermination(10, TimeUnit.HOURS);
    queryPool.awaitTermination(10, TimeUnit.HOURS);
    movePool = null;
    queryPool = null;
  }

  @JsonProperty("state")
  public FetcherState getState() {
    return state;
  }

  public void setState(FetcherState state) {
    this.state = state;
  }

}
