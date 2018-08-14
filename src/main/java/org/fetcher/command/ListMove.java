package org.fetcher.command;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import org.fetcher.Main;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "list")
public class ListMove implements Callable<Void> {

  @Override
  public Void call() throws Exception {
    List<org.fetcher.model.Move> moves = Main.queryDAO.getMoves();
    PrintWriter writer = new PrintWriter(System.out);

    StatefulBeanToCsv<org.fetcher.model.Move> sbc = new StatefulBeanToCsvBuilder<org.fetcher.model.Move>(writer)
        .withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();
    sbc.write(moves);
    writer.close();
    System.out.flush();
    return null;
  }

}
