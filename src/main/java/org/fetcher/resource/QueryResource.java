package org.fetcher.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.fetcher.Main;
import org.fetcher.model.Job;
import org.fetcher.model.Query;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import io.dropwizard.hibernate.UnitOfWork;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryResource {
  static Logger logger = LoggerFactory.getLogger(QueryResource.class);

  Job job;

  public QueryResource(Job job) {
    this.job = job;
  }

  @GET
  public JsonNode getAll() {
    List<Query> q = Main.jobDAO.getQueries(job);
    for (Query query : q) {
      logger.info(query.toString());
    }
    ObjectNode n = Main.objectMapper.createObjectNode();
    n.putPOJO("query", q);
    return n;
  }

  @POST
  @UnitOfWork
  public Query create(Query q) {
    q.setJobId(job.getJobId());
    q.queryId = Main.queryDAO.createQuery(job, q);
    return q;
  }

  @PUT
  @Path("{id}")
  @UnitOfWork
  public Query update(@PathParam("id") int queryId, Query q) {
    int count = exists(queryId);
    if (count != 1) {
      throw new WebApplicationException("query does not exist in the job", Status.NOT_FOUND);
    }
    // Main.queryDAO.merge(job, q);
    return q;
  }

  @DELETE
  @Path("{id}")
  @UnitOfWork
  public Query delete(@PathParam("id") int queryId, Query q) {
    int count = exists(queryId);
    if (count != 1) {
      throw new WebApplicationException("query does not exist in the job", Status.NOT_FOUND);
    }
    // q.setJob(job);
    // Main.queryDAO.delete(q);
    return q;
  }

  /**
   * @param queryId
   * @return
   * @throws CallbackFailedException
   */
  public int exists(int queryId) throws CallbackFailedException {
    // Check to see if it exists
    int count = Main.jdbi.withHandle(handle -> {
      return handle.createQuery("select count(*) from query where queryId = :queryId and jobId = :jobId").bind("queryId", queryId).bind("jobId", job.getJobId()).mapTo(Integer.class).first();
    });
    return count;
  }

}
