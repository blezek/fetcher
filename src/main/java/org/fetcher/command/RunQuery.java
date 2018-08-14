package org.fetcher.command;

import org.fetcher.Main;
import org.fetcher.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import me.tongfei.progressbar.ProgressBar;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "run")
public class RunQuery implements Callable<Void> {
  static Logger logger = LoggerFactory.getLogger(Query.class);

  @Option(names = { "-d", "--delete" }, description = "delete moves")
  private boolean deleteMoves = false;

  @Option(names = { "-p", "--progress" }, description = "progress bar")
  private boolean displayProgress = false;

  @Override
  public Void call() throws Exception {

    if (deleteMoves) {
      logger.debug("deleting all moves");
      Main.jdbi.useHandle(handle -> {
        handle.execute("delete from move");
      });
    }
    logger.debug("queuing all queries");
    Main.fetcher.queueAll();
    Main.fetcher.startFind();

    if (displayProgress) {
      int count = Main.queryDAO.queryCount(State.QUEUED);
      try (ProgressBar pb = new ProgressBar("query", count)) { // name, initial max

        Main.fetcher.waitForThreads(running -> {
          pb.maxHint(Main.queryDAO.queryCount());
          pb.stepTo(Main.queryDAO.queryCount(State.SUCCEEDED));
        });
      }
    }
    return null;
  }
}
