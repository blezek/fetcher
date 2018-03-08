package org.fetcher;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
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
import org.fetcher.ui.Broadcaster;
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

    @DecimalMin("0.1")
    public double queriesPerSecond = 2;
    @Min(1)
    public int concurrentQueries = 5;

    @Min(1)
    public int concurrentMoves = 5;
    @DecimalMin("0.1")
    public double imagesPerSecond = 10;

    enum FetcherState {
	STOPPED, RUNNING
    };

    FetcherState queryState = FetcherState.STOPPED;
    FetcherState moveState = FetcherState.STOPPED;

    @JsonIgnore
    public RateLimiter moveLimit;
    @JsonIgnore
    public RateLimiter queryLimit;

    @JsonIgnore
    public ThreadPoolExecutor movePool;
    @JsonIgnore
    public ThreadPoolExecutor queryPool;

    @JsonIgnore
    public LinkedBlockingQueue<Runnable> moveQueue;
    @JsonIgnore
    public LinkedBlockingQueue<Runnable> queryQueue;

    @JsonIgnore
    public Meter imageMeter;
    public Reservoir reservoir = new SlidingTimeWindowArrayReservoir(1, TimeUnit.MINUTES);

    public Fetcher() {
    }

    /**
     * @throws IllegalArgumentException
     */
    public void throwIfStateIsNot(FetcherState expected) throws IllegalArgumentException {
	if (queryState != expected) {
	    throw new IllegalArgumentException("expected state to be " + expected + " was " + queryState);
	}
    }

    @JsonProperty("status")
    public JsonNode getPools() {
	ObjectNode node = Main.objectMapper.createObjectNode();
	ObjectNode p = node.putObject("pool");
	ObjectNode n;

	n = p.putObject("find");
	n.put("active_count", queryPool.getActiveCount());
	n.put("completed_count", queryPool.getCompletedTaskCount());
	n = p.putObject("move");
	n.put("active_count", movePool.getActiveCount());
	n.put("completed_count", movePool.getCompletedTaskCount());

	p = node.putObject("query");
	Integer count = Main.jdbi.withHandle(handle -> {
	    return handle.createQuery("select count(*) from query").mapTo(Integer.TYPE).first();
	});
	p.put("count", count);
	// Breakdown by state
	for (State s : State.values()) {
	    Integer v = Main.jdbi.withHandle(handle -> {
		return handle.createQuery("select count(*) from query where status = :status")
			.bind("status", s.toString()).mapTo(Integer.TYPE).first();
	    });
	    p.put(s.toString().toLowerCase(), v);
	}
	return node;
    }

    public void queueAll() {
	throwIfStateIsNot(FetcherState.STOPPED);
	Main.jdbi.useHandle(handle -> {
	    handle.createStatement("update query set status = :status, message = :message")
		    .bind("status", State.QUEUED.toString()).bind("message", (String) null).execute();
	});
    }

    public void queue(long l) {
	throwIfStateIsNot(FetcherState.STOPPED);
	Main.jdbi.useHandle(handle -> {
	    handle.createStatement("update query set status = :status where queryId = :queryId")
		    .bind("status", State.QUEUED.toString()).bind("queryId", l).execute();
	});
    }

    public void queueAllMoves() {
	if (moveState == FetcherState.RUNNING) {
	    throw new WebApplicationException("must not be moving", Status.BAD_REQUEST);
	}
	Main.jdbi.useHandle(handle -> {
	    handle.createStatement("update move set status = :status, message = :message")
		    .bind("status", State.QUEUED.toString()).bind("message", (String) null).execute();
	});
    }

    public void queueMove(Long id) {
	if (moveState == FetcherState.RUNNING) {
	    throw new WebApplicationException("must not be moving", Status.BAD_REQUEST);
	}
	Main.jdbi.useHandle(handle -> {
	    handle.createStatement("update move set status = :status where moveId = :id")
		    .bind("status", State.QUEUED.toString()).bind("id", id).execute();
	});
    }

    @Override
    public void start() {
	moveLimit = RateLimiter.create(imagesPerSecond, 1, TimeUnit.MINUTES);
	queryLimit = RateLimiter.create(queriesPerSecond, 1, TimeUnit.MINUTES);
	moveQueue = new LinkedBlockingQueue<>();
	queryQueue = new LinkedBlockingQueue<>();
	movePool = new ThreadPoolExecutor(concurrentMoves, concurrentMoves, 0L, TimeUnit.MILLISECONDS, moveQueue);
	queryPool = new ThreadPoolExecutor(concurrentQueries, concurrentQueries, 0L, TimeUnit.MILLISECONDS, queryQueue);

	imageMeter = Main.metrics.meter("fetcher.move.image");
	Main.metrics.register("fetcher.move.images", new Gauge<Integer>() {
	    @Override
	    public Integer getValue() {
		return reservoir.getSnapshot().size();
	    }
	});
	Main.metrics.register("fetcher.move.queue", new Gauge<Integer>() {
	    @Override
	    public Integer getValue() {
		return moveQueue.size();
	    }
	});
	Main.metrics.register("fetcher.move.thread", new Gauge<Integer>() {
	    @Override
	    public Integer getValue() {
		return movePool.getActiveCount();
	    }
	});
	Main.metrics.register("fetcher.query.queue", new Gauge<Integer>() {
	    @Override
	    public Integer getValue() {
		return queryQueue.size();
	    }
	});
	Main.metrics.register("fetcher.query.thread", new Gauge<Integer>() {
	    @Override
	    public Integer getValue() {
		return queryPool.getActiveCount();
	    }
	});

	for (State state : State.values()) {
	    Main.metrics.register("fetcher.move." + state.toString().toLowerCase(),
		    new CachedGauge<Integer>(11, TimeUnit.MINUTES) {
			@Override
			protected Integer loadValue() {
			    // assume this does something which takes a long time
			    return Main.jdbi.withHandle(handle -> {
				return handle.createQuery("select count(*) from move where status = :status")
					.bind("status", state.toString()).mapTo(Integer.class).first();
			    });
			}
		    });
	    Main.metrics.register("fetcher.query." + state.toString().toLowerCase(),
		    new CachedGauge<Integer>(11, TimeUnit.MINUTES) {
			@Override
			protected Integer loadValue() {
			    // assume this does something which takes a long time
			    return Main.jdbi.withHandle(handle -> {
				return handle.createQuery("select count(*) from query where status = :status")
					.bind("status", state.toString()).mapTo(Integer.class).first();
			    });
			}
		    });
	}

    }

    public void startMove() {
	if (moveState == FetcherState.RUNNING) {
	    return;
	}
	moveState = FetcherState.RUNNING;
	// Push all queued moves into the queue
	Main.jdbi.useHandle(handle -> {

	    handle.createQuery("select moveId from move where status = :status").bind("status", State.QUEUED.toString())
		    .mapTo(Integer.class).forEach(key -> {
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
	    Main.jdbi.useHandle(handle -> {
		handle.execute("update move set status = ? where moveId = ?", State.PENDING.toString(),
			move.getMoveId());
	    });

	    CMove cMove = new CMove(this, move);
	    try {
		cMove.execute(data -> {
		    moveLimit.acquire();
		    logger.debug("Got data: " + data);

		    imageMeter.mark();
		    reservoir.update(1);
		    int completed = data.getInt(Tag.NumberOfCompletedSuboperations, 0);
		    int total = completed + data.getInt(Tag.NumberOfRemainingSuboperations, 0);
		    String imagesMoved = completed + " / " + total + " images moved";
		    String msg = imagesMoved + " -- " + move.getPatientId() + " / " + move.getPatientName() + " / "
			    + move.getAccessionNumber();
		    if (Broadcaster.broadcast(msg)) {
			Main.jdbi.useHandle(handle -> {
			    handle.execute("update move set message = ? where moveId = ?", imagesMoved,
				    move.getMoveId());
			});
		    }
		    // Return true if we are running
		    return moveState != FetcherState.STOPPED;
		});
		Main.jdbi.useHandle(handle -> {
		    handle.execute("update move set status = ?, message = ? where moveId = ?",
			    State.SUCCEEDED.toString(), "", move.getMoveId());
		});
		Broadcaster.broadcast("moved completed " + move.getPatientId() + " / " + move.getPatientName() + " / "
			+ move.getAccessionNumber());
	    } catch (Exception e) {
		logger.error("Error in move", e);
		Main.jdbi.useHandle(handle -> {
		    handle.execute("update move set status = ?, message = ? where moveId = ?", State.FAILED.toString(),
			    e.getLocalizedMessage(), move.getMoveId());
		});
	    }
	});
    }

    public void startFind() {

	if (queryState == FetcherState.RUNNING) {
	    return;
	}
	queryState = FetcherState.RUNNING;

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
			Move m = new Move();
			m.setQueryId(query.getQueryId());
			m.setStudyInstanceUID(data.getString(Tag.StudyInstanceUID, (String) null));
			m.setSeriesInstanceUID(data.getString(Tag.SeriesInstanceUID, (String) null));
			m.setPatientId(data.getString(Tag.PatientID, null));
			m.setPatientName(data.getString(Tag.PatientName, null));
			m.setAccessionNumber(data.getString(Tag.AccessionNumber, null));
			m.setStatus(State.CREATED.toString());
			m.setQueryRetrieveLevel(query.getQueryRetrieveLevel());

			Broadcaster.broadcast("Creating move for " + m.getPatientId() + " / " + m.getPatientName()
				+ " / " + m.getAccessionNumber());
			int key = Main.queryDAO.createMove(m);
			if (moveState == FetcherState.RUNNING) {
			    queueMove(key);
			}
			return moveState != FetcherState.STOPPED;
		    });
		    query.status = State.SUCCEEDED.toString();
		    Main.queryDAO.update(query);
		} catch (Exception e) {
		    query.status = State.FAILED.toString();
		    query.message = e.getMessage();
		    logger.error("Error in query", e);
		    Main.queryDAO.update(query);
		}
	    });
	}
    }

    public void stopFind() {
	ArrayList<Runnable> drain = new ArrayList<>();
	moveQueue.drainTo(drain);
	queryState = FetcherState.STOPPED;
    }

    /**
     * Stop processing and wait for all pools to stop
     * 
     * @throws InterruptedException
     */
    @Override
    public void stop() throws InterruptedException {

	movePool.shutdownNow();
	queryPool.shutdownNow();
	movePool.awaitTermination(10, TimeUnit.HOURS);
	queryPool.awaitTermination(10, TimeUnit.HOURS);
	movePool = null;
	queryPool = null;
    }

    @JsonProperty("findState")
    public FetcherState getFindState() {
	return queryState;
    }

    @JsonProperty("moveState")
    public FetcherState getMoveState() {
	return moveState;
    }

    public void setState(FetcherState state) {
	this.queryState = state;
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

    public RateLimiter getMoveLimit() {
	return moveLimit;
    }

    public boolean isQueryRunning() {
	return queryState == FetcherState.RUNNING;
    }

    public boolean isMoveRunning() {
	return moveState == FetcherState.RUNNING;
    }

}
