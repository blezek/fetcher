package org.fetcher.command;

import org.fetcher.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "delete")
public class DeleteQuery implements Callable<Void> {

  @Parameters
  List<Integer> keys = new ArrayList<>();

  @Override
  public Void call() throws Exception {
    if (keys.size() == 0) {
      Main.jdbi.open().execute("delete from query");
    } else {
      keys.forEach(key -> {
        Main.jdbi.useHandle(handle -> {
          handle.execute("delete from query where queryId = ?", key);
          handle.execute("delete from move where queryId = ?", key);
        });
      });
    }
    return null;
  }

}
