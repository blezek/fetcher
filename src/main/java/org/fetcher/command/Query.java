package org.fetcher.command;

import org.fetcher.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "query", subcommands = { LoadQuery.class, RunQuery.class, DeleteQuery.class, ListQuery.class })
public class Query implements Callable<Void> {
  static Logger logger = LoggerFactory.getLogger(Query.class);

  @Override
  public Void call() throws Exception {
    // Double check the Fetcher is created
    if (Main.fetcher == null) {
      logger.error("Fetcher has not been created");
      System.exit(0);
    }
    return null;
  }

}
