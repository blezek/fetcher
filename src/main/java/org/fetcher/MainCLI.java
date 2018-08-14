package org.fetcher;

import org.apache.commons.dbcp2.BasicDataSource;
import org.fetcher.command.Move;
import org.fetcher.command.Query;
import org.fetcher.model.QueryDAO;
import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Callable;

import ch.qos.logback.classic.Level;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.RunAll;

public class MainCLI implements Callable<Void> {
  static Logger logger = LoggerFactory.getLogger(MainCLI.class);

  @Option(names = { "-v", "--verbose" }, description = "Be verbose.")
  private boolean verbose = false;

  @Option(names = { "--verbose-dicom" }, description = "Log DICOM results")
  private boolean verboseDICOM = false;

  @Option(names = { "-d", "--database" }, description = "SQLite Database file, default is fetcher.db")
  private File database = new File("fetcher.db");

  @Mixin
  private Fetcher fetcher;

  public static void main(String[] args) throws Exception {
    CommandLine cmd = new CommandLine(new MainCLI());
    cmd.addSubcommand("query", new Query());
    cmd.addSubcommand("move", new Move());
    int status = 0;
    try {
      cmd.parseWithHandler(new RunAll(), args);
    } catch (Exception e) {
      logger.error("error running command", e);
      status = 1;
    }
    try {
      Main.fetcher.waitForThreads();
      Main.fetcher.stop();
    } catch (Exception e) {
      logger.error("error stopping fetcher", e);
      status = 1;
    }
    System.exit(status);

  }

  @Override
  public Void call() throws Exception {

    Logger l = LoggerFactory.getLogger("org.dcm4che3");
    if (l instanceof ch.qos.logback.classic.Logger) {
      ch.qos.logback.classic.Logger ll = (ch.qos.logback.classic.Logger) l;
      if (!verboseDICOM) {
        ll.setLevel(Level.ERROR);
      }
    }
    l = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    if (l instanceof ch.qos.logback.classic.Logger) {
      ch.qos.logback.classic.Logger ll = (ch.qos.logback.classic.Logger) l;
      if (verbose) {
        ll.setLevel(Level.DEBUG);
      } else {
        ll.setLevel(Level.INFO);
      }
    }
    l = LoggerFactory.getLogger("org.flywaydb");
    if (l instanceof ch.qos.logback.classic.Logger) {
      ch.qos.logback.classic.Logger ll = (ch.qos.logback.classic.Logger) l;
      ll.setLevel(Level.ERROR);
    }

    // SQLiteDataSource ds = new SQLiteDataSource();
    BasicDataSource ds = new BasicDataSource();
    ds.setUrl("jdbc:sqlite:" + database.getAbsolutePath());
    // this is rather important for SQLite, it doesn't handle concurrency
    // but write statements will queue up as necessary
    ds.setDefaultQueryTimeout(10);
    Flyway flyway = new Flyway();
    flyway.setLocations("db.sqlite.migration");
    flyway.setDataSource(ds);
    flyway.migrate();

    Main.jdbi = new DBI(ds);

    Main.jdbi.useHandle(handle -> {
      handle.execute("pragma busy_timeout=30000;");
    });

    Main.queryDAO = Main.jdbi.onDemand(QueryDAO.class);
    Main.fetcher = this.fetcher;

    Main.fetcher.start();

    return null;
  }
}
