package org.fetcher.command;

import org.fetcher.Main;
import org.fetcher.ui.UploadCSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "load")
public class LoadQuery implements Callable<Void> {
  static Logger logger = LoggerFactory.getLogger(Query.class);

  @Parameters(index = "0")
  private File inputFiles;

  @Override
  public Void call() throws Exception {
    List<org.fetcher.model.Query> queries = UploadCSV.loadQueries(new FileInputStream(inputFiles));
    queries.forEach(it -> {
      it.setQueryRetrieveLevel("STUDY");
      Main.queryDAO.createQuery(it);
    });
    return null;
  }
}
