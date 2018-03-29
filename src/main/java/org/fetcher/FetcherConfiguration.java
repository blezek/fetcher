package org.fetcher;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

public class FetcherConfiguration extends Configuration {
  @NotNull
  @Valid
  private DataSourceFactory dataSourceFactory = new DataSourceFactory();

  @NotNull
  @Valid
  private Fetcher fetcher = new Fetcher();

  /**
   * A getter for the database factory.
   *
   * @return An instance of database factory deserialized from the configuration
   *         file passed as a command-line argument to the application.
   */
  @JsonProperty("database")
  public DataSourceFactory getDataSourceFactory() {
    return dataSourceFactory;
  }

  @JsonProperty("fetch")
  public Fetcher getFetcher() {
    return fetcher;
  }

}
