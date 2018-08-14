package org.fetcher.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "move", subcommands = { QueueMove.class, RunMove.class, DeleteMove.class, ListMove.class })
public class Move implements Callable<Void> {
  static Logger logger = LoggerFactory.getLogger(Move.class);

  @Override
  public Void call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
