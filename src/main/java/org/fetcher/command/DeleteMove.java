package org.fetcher.command;

import org.fetcher.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "delete")
public class DeleteMove implements Callable<Void> {

  @Parameters
  List<Integer> keys = new ArrayList<>();

  @Override
  public Void call() throws Exception {
    if (keys.size() == 0) {
      Main.jdbi.useHandle(handle -> {
        handle.execute("delete from move");
      });
    } else {
      keys.forEach(key -> {
        Main.jdbi.useHandle(handle -> {
          handle.execute("delete from move where moveId = ?", key);
        });
      });
    }
    return null;
  }

}
