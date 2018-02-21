package org.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.fetcher.model.JobDAO;
import org.fetcher.model.QueryDAO;
import org.fetcher.resource.JobResource;
import org.skife.jdbi.v2.DBI;

import java.io.File;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Main extends Application<FetcherConfiguration> {

  public static DBI jdbi;
  static File homePath = null;
  public static JobDAO jobDAO;
  public static ObjectMapper objectMapper;

  public static void main(String[] args) throws Exception {
    try {
      homePath = new File(args[1]).getParentFile();
      new Main().run(args);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "dewey";
  }

  @Override
  public void initialize(Bootstrap<FetcherConfiguration> bootstrap) {
    bootstrap.addBundle(new FlywayBundle());
    bootstrap.addBundle(new MultiPartBundle());
    bootstrap.addBundle(new WebDBConsole());
  }

  public static QueryDAO queryDAO;

  @Override
  public void run(FetcherConfiguration configuration, Environment environment) throws Exception {
    jdbi = new DBIFactory().build(environment, configuration.getDataSourceFactory(), "jbdi");
    queryDAO = jdbi.onDemand(QueryDAO.class);
    jobDAO = jdbi.onDemand(JobDAO.class);

    JobManager jobManager = new JobManager();
    environment.jersey().register(new JobResource(jobManager));

    environment.lifecycle().manage(jobManager);
    objectMapper = environment.getObjectMapper();
    environment.jersey().register(new JsonProcessingExceptionMapper(true));

  }

}
