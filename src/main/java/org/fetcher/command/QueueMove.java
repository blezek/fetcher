package org.fetcher.command;

import org.fetcher.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "queue")
public class QueueMove implements Callable<Void> {
  static Logger logger = LoggerFactory.getLogger(Move.class);

  @Override
  public Void call() throws Exception {
    Main.fetcher.queueAllMoves();
    return null;
  }

}
