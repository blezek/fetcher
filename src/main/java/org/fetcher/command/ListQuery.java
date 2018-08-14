package org.fetcher.command;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import org.fetcher.Main;
import org.fetcher.model.Query;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "list")
public class ListQuery implements Callable<Void> {

  @Override
  public Void call() throws Exception {
    List<Query> queries = Main.queryDAO.getQueries();
    PrintWriter writer = new PrintWriter(System.out);

    StatefulBeanToCsv<Query> sbc = new StatefulBeanToCsvBuilder<Query>(writer)
        .withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();
    sbc.write(queries);
    writer.close();
    System.out.flush();
    return null;
  }

}
