package org.fetcher;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.RateLimiter;

import org.dcm4che3.data.Tag;
import org.fetcher.dicom.CFind;
import org.fetcher.dicom.CMove;
import org.fetcher.model.Move;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.dropwizard.lifecycle.Managed;

public class Fetcher implements Managed {
  static Logger logger = LoggerFactory.getLogger(Fetcher.class);

  public String calledAET = "pacs";
  @Min(1)
  @Max(65535)
  public int calledPort = 1234;
  public String hostname = "example.com";
  public String callingAET = "fetcher";
  public String destinationAET = "fetcher";

  @JsonIgnore
  public String fetchBy = "SERIES";

  @Min(1)
  public int queriesPerSecond = 2;
  @Min(1)
  public int concurrentQueries = 5;

  @Min(1)
  public int concurrentMoves = 5;
  @Min(1)
  public int imagesPerSecond = 100;

  enum FetcherState {
    STOPPED, RUNNING
  };

  FetcherState findState = FetcherState.STOPPED;
  FetcherState moveState = FetcherState.STOPPED;

  RateLimiter moveLimit;
  RateLimiter queryLimit;

  ThreadPoolExecutor movePool;
  ThreadPoolExecutor queryPool;

  private LinkedBlockingQueue<Runnable> moveQueue;

  public Fetcher() {
  }

  /**
   * @throws IllegalArgumentException
   */
  public void throwIfStateIsNot(FetcherState expected) throws IllegalArgumentException {
    if (findState != expected) {
      throw new IllegalArgumentException("expected state to be " + expected + " was " + findState);
    }
  }

  @JsonProperty("status")
  public JsonNode getPools() {
    ObjectNode node = Main.objectMapper.createObjectNode();
    ObjectNode p = node.putObject("pool");
    if (findState == FetcherState.RUNNING) {
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
      return handle.createQuery("select count(*) from query").mapTo(Integer.TYPE).first();
    }));
    // Breakdown by state
    for (State s : State.values()) {
      p.put(s.toString().toLowerCase(), Main.jdbi.withHandle(handle -> {
        return handle.createQuery("select count(*) from query where status = :status").bind("status", s.toString()).mapTo(Integer.TYPE).first();
      }));
    }
    return node;
  }

  public void queueAll() {
    throwIfStateIsNot(FetcherState.STOPPED);
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update query set status = :status, message = :message").bind("status", State.QUEUED.toString()).bind("message", (String) null).execute();
    });
  }

  public void queue(int qid) {
    throwIfStateIsNot(FetcherState.STOPPED);
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update query set status = :status where queueId = :queue_id").bind("status", State.QUEUED.toString()).bind("queue_id", qid).execute();
    });
  }

  public void queueAllMoves() {
    if (moveState == FetcherState.RUNNING) {
      throw new WebApplicationException("must not be moving", Status.BAD_REQUEST);
    }
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update move set status = :status, message = :message").bind("status", State.QUEUED.toString()).bind("message", (String) null).execute();
    });
  }

  public void queueMove(int id) {
    if (moveState == FetcherState.RUNNING) {
      throw new WebApplicationException("must not be moving", Status.BAD_REQUEST);
    }
    Main.jdbi.useHandle(handle -> {
      handle.createStatement("update move set status = :status where moveId = :id").bind("status", State.QUEUED.toString()).bind("id", id).execute();
    });
  }

  @Override
  public void start() {
    moveLimit = RateLimiter.create(imagesPerSecond);
    queryLimit = RateLimiter.create(queriesPerSecond);
    moveQueue = new LinkedBlockingQueue<>();
    movePool = new ThreadPoolExecutor(concurrentMoves, concurrentMoves, 0L, TimeUnit.MILLISECONDS, moveQueue);
    queryPool = new ThreadPoolExecutor(concurrentQueries, concurrentQueries, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public void startMove() {
    if (moveState == FetcherState.RUNNING) {
      return;
    }
    // Push all queued moves into the queue
    Main.jdbi.useHandle(handle -> {

      handle.createQuery("select moveId from move where status = :status").bind("status", State.QUEUED.toString()).mapTo(Integer.class).forEach(key -> {
        queueMove(key);
      });

    });
  }

  public void stopMove() {
    moveState = FetcherState.STOPPED;
    ArrayList<Runnable> drain = new ArrayList<>();
    moveQueue.drainTo(drain);
  }

  private void queueMove(Integer key) {
    movePool.execute(() -> {
      logger.info("move " + key);
      Move move = Main.queryDAO.getMove(key);
      moveLimit.acquire(move.getNumberOfSeriesRelatedInstances());
      CMove cMove = new CMove(this, move);
      try {
        cMove.execute(data -> {
          logger.info("Got data: " + data);
        });
        Main.jdbi.useHandle(handle -> {
          handle.execute("update move set status = ? where moveId = ?", State.SUCCEEDED.toString(), move.getMoveId());
        });
      } catch (Exception e) {
        logger.error("Error in move", e);
        Main.jdbi.useHandle(handle -> {
          handle.execute("update move set status = ?, message = ? where moveId = ?", State.SUCCEEDED.toString(), e.getLocalizedMessage(), move.getMoveId());
        });
      }
    });
  }

  public void startFind() {

    if (findState == FetcherState.RUNNING) {
      return;
    }
    findState = FetcherState.RUNNING;

    // Push them all into the queue
    for (Query query : Main.queryDAO.getQueries()) {

      queryPool.execute(() -> {
        // Execute the DICOM query
        logger.info("query for " + query);
        queryLimit.acquire();
        Main.jdbi.useHandle(handle -> {
          handle.update("delete from move where queryId = ?", query.queryId);
        });
        CFind find = new CFind(this, query);
        try {
          find.execute(data -> {
            logger.info("Got data: " + data);
            Main.jdbi.useHandle(handle -> {
              Move m = new Move();
              m.setQueryId(query.getQueryId());
              m.setStudyInstanceUID(data.getString(Tag.StudyInstanceUID, (String) null));
              m.setSeriesInstanceUID(data.getString(Tag.SeriesInstanceUID, (String) null));
              m.setNumberOfSeriesRelatedInstances(data.getInt(Tag.NumberOfSeriesRelatedInstances, 0));
              m.setStatus(State.CREATED.toString());

              int key = Main.queryDAO.createMove(m);
              if (moveState == FetcherState.RUNNING) {
                queueMove(key);
              }
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
  @Override
  public void stop() throws InterruptedException {
    if (findState == FetcherState.STOPPED) {
      return;
    }

    findState = FetcherState.STOPPED;

    movePool.shutdownNow();
    queryPool.shutdownNow();
    movePool.awaitTermination(10, TimeUnit.HOURS);
    queryPool.awaitTermination(10, TimeUnit.HOURS);
    movePool = null;
    queryPool = null;
  }

  @JsonProperty("findState")
  public FetcherState getFindState() {
    return findState;
  }

  @JsonProperty("moveState")
  public FetcherState getMoveState() {
    return moveState;
  }

  public void setState(FetcherState state) {
    this.findState = state;
  }

  public String getCalledAET() {
    return calledAET;
  }

  public void setCalledAET(String calledAET) {
    this.calledAET = calledAET;
  }

  public int getCalledPort() {
    return calledPort;
  }

  public void setCalledPort(int calledPort) {
    this.calledPort = calledPort;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getCallingAET() {
    return callingAET;
  }

  public void setCallingAET(String callingAET) {
    this.callingAET = callingAET;
  }

  public String getDestinationAET() {
    return destinationAET;
  }

  public void setDestinationAET(String destinationAET) {
    this.destinationAET = destinationAET;
  }

  public String getFetchBy() {
    return fetchBy;
  }

  public void setFetchBy(String fetchBy) {
    this.fetchBy = fetchBy;
  }

  public int getConcurrentQueries() {
    return concurrentQueries;
  }

  public void setConcurrentQueries(int concurrentQueries) {
    this.concurrentQueries = concurrentQueries;
  }

  public int getConcurrentMoves() {
    return concurrentMoves;
  }

  public void setConcurrentMoves(int concurrentMoves) {
    this.concurrentMoves = concurrentMoves;
  }

}
