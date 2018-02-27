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

import org.fetcher.Fetcher;
import org.fetcher.Main;
import org.fetcher.State;
import org.fetcher.model.Move;
import org.fetcher.model.Query;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("query")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryResource {
  static Logger logger = LoggerFactory.getLogger(QueryResource.class);
  private Fetcher fetcher;

  public QueryResource(Fetcher fetcher) {
    this.fetcher = fetcher;
  }

  @GET
  public JsonNode getAll() {
    List<Query> q = Main.queryDAO.getQueries();
    for (Query query : q) {
      logger.info(query.toString());
    }
    ObjectNode n = Main.objectMapper.createObjectNode();
    n.putPOJO("query", q);
    return n;
  }

  @POST
  public Query create(Query q) {
    q.status = State.CREATED.toString();
    q.queryId = Main.queryDAO.createQuery(q);
    return q;
  }

  @PUT
  @Path("{id}")
  public Query update(@PathParam("id") int queryId, Query q) {
    int count = exists(queryId);
    if (count != 1) {
      throw new WebApplicationException("query does not exist in the job", Status.NOT_FOUND);
    }
    Main.queryDAO.update(q);
    return q;
  }

  @GET
  @Path("{id}/moves")
  public JsonNode getMoves(@PathParam("id") int queryId) {
    int count = exists(queryId);
    if (count != 1) {
      throw new WebApplicationException("query does not exist", Status.NOT_FOUND);
    }
    List<Move> q = Main.queryDAO.getMoves(queryId);
    ObjectNode n = Main.objectMapper.createObjectNode();
    n.putPOJO("query", q);
    return n;
  }

  @DELETE
  @Path("{id}")
  public String delete(@PathParam("id") long queryId) {
    Main.queryDAO.delete(queryId);
    return "deleted";
  }

  /**
   * @param queryId
   * @return
   * @throws CallbackFailedException
   */
  public int exists(int queryId) throws CallbackFailedException {
    // Check to see if it exists
    int count = Main.jdbi.withHandle(handle -> {
      return handle.createQuery("select count(*) from query where queryId = :queryId").bind("queryId", queryId).mapTo(Integer.class).first();
    });
    return count;
  }

}
