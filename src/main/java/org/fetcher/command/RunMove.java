package org.fetcher.command;

import org.fetcher.Fetcher;
import org.fetcher.Main;
import org.fetcher.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import me.tongfei.progressbar.ProgressBar;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "run")
public class RunMove implements Callable<Void> {
  static Logger logger = LoggerFactory.getLogger(Move.class);

  @Option(names = { "-p", "--progress" }, description = "progress bar")
  private boolean displayProgress = false;

  @Option(names = { "-q", "--queue" }, description = "queue all moves (otherwise continue)")
  private boolean queueMoves = false;

  @Override
  public Void call() throws Exception {
    logger.debug("queuing all queries");
    if (queueMoves) {
      Main.fetcher.queueAllMoves();
    }
    Main.fetcher.startMove();
    if (displayProgress) {
      int count = Main.queryDAO.moveCount(State.QUEUED);
      try (ProgressBar pb = new ProgressBar("move", count)) { // name, initial max

        Main.fetcher.waitForThreads(running -> {
          Fetcher fetcher = Main.fetcher;
          String t = fetcher.reservoir.getSnapshot().size() + " images per minute";
          pb.setExtraMessage(t);
          pb.stepTo(Main.queryDAO.moveCount(State.SUCCEEDED));
        });
      }
    }
    return null;
  }

}
