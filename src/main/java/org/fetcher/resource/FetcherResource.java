package org.fetcher.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.fetcher.Fetcher;

@Path("fetch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FetcherResource {

  Fetcher fetcher;

  public FetcherResource(Fetcher fetcher) {
    this.fetcher = fetcher;
  }

  @GET
  public Fetcher get() {
    return fetcher;
  }

  @PUT
  @Path("find/queue")
  public Fetcher queueAll() {
    try {
      fetcher.queueAll();
    } catch (Exception e) {
      throw new WebApplicationException(e.getLocalizedMessage(), Status.INTERNAL_SERVER_ERROR);
    }
    return fetcher;
  }

  @PUT
  @Path("find/queue/{qid}")
  public Fetcher queueAll(@PathParam("id") int qid) {
    fetcher.queue(qid);
    return fetcher;
  }

  @PUT
  @Path("move/queue")
  public Fetcher queueAllMoves() {
    try {
      fetcher.queueAllMoves();
    } catch (Exception e) {
      throw new WebApplicationException(e.getLocalizedMessage(), Status.INTERNAL_SERVER_ERROR);
    }
    return fetcher;
  }

  @PUT
  @Path("move/queue/{qid}")
  public Fetcher queueMove(@PathParam("id") long qid) {
    fetcher.queueMove(qid);
    return fetcher;
  }

  @PUT
  @Path("find/start")
  public Fetcher start() {
    fetcher.startFind();
    return fetcher;
  }

  @PUT
  @Path("move/start")
  public Fetcher startMove() {
    fetcher.startMove();
    return fetcher;
  }

  @PUT
  @Path("find/stop")
  public Fetcher stop() throws InterruptedException {
    fetcher.stopFind();
    return fetcher;
  }

  @PUT
  @Path("move/stop")
  public Fetcher stopMove() throws InterruptedException {
    fetcher.stopMove();
    return fetcher;
  }
}
