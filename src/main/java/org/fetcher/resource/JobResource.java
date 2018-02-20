package org.fetcher.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.fetcher.Fetcher;
import org.fetcher.JobManager;
import org.fetcher.Main;
import org.fetcher.model.Job;

import io.dropwizard.hibernate.UnitOfWork;

@Path("job")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobResource {

  JobManager manager;

  public JobResource(JobManager manager) {
    this.manager = manager;
  }

  @GET
  @Path("{id}")
  @UnitOfWork
  public Fetcher getById(@PathParam("id") int id) {
    return manager.getById(id);
  }

  @PUT
  @Path("{id}/start")
  @UnitOfWork
  public Fetcher start(@PathParam("id") int id) {
    Fetcher f = manager.getById(id);
    f.start();
    return f;
  }

  @PUT
  @Path("{id}/stop")
  @UnitOfWork
  public Fetcher stop(@PathParam("id") int id) throws InterruptedException {
    Fetcher f = manager.getById(id);
    f.stop();
    return f;
  }

  @PUT
  @Path("{id}/queue")
  public Fetcher queueAll(@PathParam("id") int id) {
    Fetcher f = manager.getById(id);
    f.queueAll();
    return f;
  }

  @PUT
  @Path("{id}/queue/{qid}")
  public Fetcher queueAll(@PathParam("id") int id, @PathParam("id") int qid) {
    Fetcher f = manager.getById(id);
    f.queue(qid);
    return f;
  }

  @GET
  public JsonNode get() {
    ObjectNode node = Main.objectMapper.createObjectNode();
    ArrayNode a = node.putArray("jobs");
    for (Fetcher fetcher : manager.getAll()) {
      a.addPOJO(fetcher);
    }
    return node;
  }

  @POST
  @UnitOfWork
  public Fetcher create(Job job) {
    return manager.create(job);
  }

  @Path("{id}/query")
  @UnitOfWork
  public QueryResource getQuery(@PathParam("id") int id) {
    Fetcher f = manager.getById(id);
    if (f == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return new QueryResource(f.getJob());
  }

}
