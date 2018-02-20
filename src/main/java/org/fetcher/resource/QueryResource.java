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

import java.util.Set;

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
  @UnitOfWork
  public JsonNode getAll() {
    job = Main.jobDAO.update(job);
    Set<Query> q = job.getQueries();
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

    q.setJob(job);
    Main.queueDAO.create(q);
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
    q.setJob(job);
    Main.queueDAO.merge(q);
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
    q.setJob(job);
    Main.queueDAO.delete(q);
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
      return handle.createQuery("select count(*) from query where query_id = :query_id and job_id = :job_id").bind("query_id", queryId).bind("job_id", job.getJobId()).mapTo(Integer.class).first();
    });
    return count;
  }

}
