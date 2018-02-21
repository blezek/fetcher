package org.fetcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.RateLimiter;

import org.dcm4che3.data.Tag;
import org.fetcher.dicom.CFind;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Fetcher {
  static Logger logger = LoggerFactory.getLogger(Fetcher.class);

  public String name = "default";
  public String calledAET = "pacs";
  public int calledPort = 1234;
  public String hostname = "example.com";
  public String callingAET = "fetcher";
  public String destinationAET = "fetcher";
  public String fetchBy = "STUDY";
  public int queriesPerSecond = 2;
  public int concurrentQueries = 5;
  public int movesPerSecond = 2;
  public int concurrentMoves = 5;

  enum FetcherState {
    STOPPED, RUNNING
  };

  FetcherState state = FetcherState.STOPPED;

  RateLimiter moveLimit;
  RateLimiter queryLimit;

  ThreadPoolExecutor movePool;
  ThreadPoolExecutor queryPool;

  public Fetcher() {
  }

  /**
   * @throws IllegalArgumentException
   */
  public void throwIfStateIsNot(FetcherState expected) throws IllegalArgumentException {
    if (state != expected) {
      throw new IllegalArgumentException("expected state to be " + expected + " was " + state);
    }
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
      return handle.createQuery("select count(*) from query where jobId = ?").bind(0, jobId).mapTo(Integer.TYPE).first();
    }));
    // Breakdown by state
    for (State s : State.values()) {
      p.put(s.toString().toLowerCase(), Main.jdbi.withHandle(handle -> {
        return handle.createQuery("select count(*) from query where jobId = ? and status = ?").bind(0, jobId).bind(1, s.toString()).mapTo(Integer.TYPE).first();
      }));
    }
    return node;
  }

  public void queueAll() {
    throwIfStateIsNot(FetcherState.STOPPED);
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update query set status = :status, message = :message where jobId = :jobId").bind("status", State.QUEUED.toString()).bind("jobId", jobId).bind("message", (String) null).execute();
    });
  }

  public void queue(int qid) {
    throwIfStateIsNot(FetcherState.STOPPED);
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update query set status = :status where jobId = :jobId and queue_id = :queue_id").bind("status", State.SUCCEEDED.toString()).bind("jobId", jobId).bind("queue_id", qid).execute();
    });
  }

  public void start() {

    if (state == FetcherState.RUNNING) {
      return;
    }
    state = FetcherState.RUNNING;

    moveLimit = RateLimiter.create(getMovesPerSecond());
    queryLimit = RateLimiter.create(getQueriesPerSecond());
    movePool = new ThreadPoolExecutor(getConcurrentMoves(), getConcurrentMoves(), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    queryPool = new ThreadPoolExecutor(getConcurrentQueries(), getConcurrentQueries(), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    // Push them all into the queue
    for (Query query : Main.jobDAO.getQueries(job)) {
      queryPool.execute(() -> {
        // Execute the DICOM query
        logger.info("query for " + query);
        Main.jdbi.useHandle(handle -> {
          handle.update("delete from move where queryId = ?", query.queryId);
        });
        CFind find = new CFind(job, query);
        try {
          find.execute(data -> {
            logger.info("Got data: " + data);
            Main.jdbi.useHandle(handle -> {
              handle.createStatement("insert into move (jobId, queryId, studyInstanceUID, seriesInstanceUID, status ) values ( ?, ?, ?, ?, ? )").bind(0, getJobId()).bind(1, query.getQueryId()).bind(2, data.getString(Tag.StudyInstanceUID, (String) null))
                  .bind(3, data.getString(Tag.SeriesInstanceUID, (String) null)).bind(4, State.CREATED.toString()).execute();
            });
          });
        } catch (Exception e) {
          query.status = State.FAILED.toString();
          query.message = e.getMessage();
          logger.error("Error in query", e);
          Main.queryDAO.update(query);
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
