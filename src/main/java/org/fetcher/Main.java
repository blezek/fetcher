package org.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.fetcher.model.Job;
import org.fetcher.model.JobDAO;
import org.fetcher.model.Move;
import org.fetcher.model.Query;
import org.fetcher.model.QueryDAO;
import org.fetcher.resource.JobResource;
import org.skife.jdbi.v2.DBI;

import java.io.File;

import io.dropwizard.Application;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Main extends Application<FetcherConfiguration> {

  public static DBI jdbi;
  static File homePath = null;
  HibernateBundle<FetcherConfiguration> hibernateBundle;
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
    hibernateBundle = new HibernateBundle<FetcherConfiguration>(Query.class, Job.class, Move.class) {
      @Override
      public PooledDataSourceFactory getDataSourceFactory(FetcherConfiguration configuration) {
        return configuration.getDataSourceFactory();
      }
    };
    bootstrap.addBundle(hibernateBundle);
  }

  public static QueryDAO queueDAO;

  @Override
  public void run(FetcherConfiguration configuration, Environment environment) throws Exception {
    queueDAO = new QueryDAO(hibernateBundle.getSessionFactory());
    jobDAO = new JobDAO(hibernateBundle.getSessionFactory());

    JobManager jobManager = new UnitOfWorkAwareProxyFactory(hibernateBundle).create(JobManager.class, JobDAO.class, jobDAO);
    environment.jersey().register(new JobResource(jobManager));

    environment.lifecycle().manage(jobManager);
    jdbi = new DBIFactory().build(environment, configuration.getDataSourceFactory(), "jbdi");
    objectMapper = environment.getObjectMapper();
  }

}
