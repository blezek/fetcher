package org.fetcher;

import org.h2.server.web.WebServlet;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class WebDBConsole implements Bundle {

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
  }

  @Override
  public void run(Environment environment) {
    environment.getApplicationContext().addServlet(WebServlet.class, "/console/*");
  }

}