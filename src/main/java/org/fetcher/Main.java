package org.fetcher;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import org.eclipse.jetty.server.session.SessionHandler;
import org.fetcher.model.QueryDAO;
import org.fetcher.resource.FetcherResource;
import org.fetcher.resource.QueryResource;
import org.fetcher.ui.FetcherUI;
import org.skife.jdbi.v2.DBI;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;

public class Main extends Application<FetcherConfiguration> {
    public static Fetcher fetcher;
    public static DBI jdbi;
    public static ObjectMapper objectMapper;
    public static MetricRegistry metrics;

    public static void main(String[] args) throws Exception {
	try {
	    // homePath = new File(args[1]).getParentFile();
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
	bootstrap.addBundle(new AssetsBundle("/VAADIN", "/VAADIN", null, "vaadin"));

    }

    public static QueryDAO queryDAO;

    @Override
    public void run(FetcherConfiguration configuration, Environment environment) throws Exception {

	metrics = environment.metrics();
	CollectorRegistry.defaultRegistry.register(new DropwizardExports(environment.metrics()));
	environment.servlets().addServlet("prometheus", new MetricsServlet()).addMapping("/prometheus/*");

	jdbi = new DBIFactory().build(environment, configuration.getDataSourceFactory(), "jbdi");
	queryDAO = jdbi.onDemand(QueryDAO.class);

	environment.jersey().register(new FetcherResource(configuration.getFetcher()));
	environment.jersey().register(new QueryResource());

	fetcher = configuration.getFetcher();
	environment.lifecycle().manage(configuration.getFetcher());
	objectMapper = environment.getObjectMapper();
	environment.jersey().register(new JsonProcessingExceptionMapper(true));

	// Add a session handler
	environment.servlets().setSessionHandler(new SessionHandler());
	environment.getApplicationContext().addServlet(FetcherVaadinServlet.class, "/ui/*");

    }

    @VaadinServletConfiguration(ui = FetcherUI.class, productionMode = false)
    public static class FetcherVaadinServlet extends VaadinServlet {
	private static final long serialVersionUID = 1L;
    }

}
