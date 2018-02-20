package org.fetcher;

import org.flywaydb.core.Flyway;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class FlywayBundle implements ConfiguredBundle<FetcherConfiguration> {

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
  }

  @Override
  public void run(FetcherConfiguration configuration, Environment environment) throws Exception {
    Flyway flyway = new Flyway();
    flyway.setDataSource(configuration.getDataSourceFactory().build(environment.metrics(), "Flyway"));
    flyway.migrate();

  }

}
