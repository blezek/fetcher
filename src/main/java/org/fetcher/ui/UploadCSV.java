package org.fetcher.ui;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.fetcher.Main;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class UploadCSV implements Receiver, SucceededListener {
  static Logger logger = LoggerFactory.getLogger(UploadCSV.class);
  ByteArrayOutputStream buffer;
  UI ui;

  public UploadCSV(UI ui) {
    this.ui = ui;
  }

  @Override
  public OutputStream receiveUpload(String filename, String mimeType) {
    // Can we read it into a csv file
    logger.info("Uploaded a file: " + filename + " of type " + mimeType);
    buffer = new ByteArrayOutputStream();
    return buffer;
  }

  @Override
  public void uploadSucceeded(SucceededEvent event) {
    List<Query> queries = new ArrayList<>();
    // Load the CSV
    ByteArrayInputStream out = new ByteArrayInputStream(buffer.toByteArray());
    try (InputStreamReader in = new InputStreamReader(out)) {
      // CSVReader csvReader = new
      // CSVReaderBuilder(in).withSkipLines(1).build();
      ColumnPositionMappingStrategy<Query> strategy = new ColumnPositionMappingStrategy<>();
      strategy.setType(Query.class);
      String[] memberFieldsToBindTo = { "patientId", "patientName", "accessionNumber", "studyDate", "queryRetrieveLevel" };
      strategy.setColumnMapping(memberFieldsToBindTo);

      CsvToBean<Query> csvToBean = new CsvToBeanBuilder<Query>(in).withMappingStrategy(strategy).withSkipLines(1).withIgnoreLeadingWhiteSpace(true).build();
      queries = csvToBean.parse();
      queries.forEach(it -> {
        if (it.getQueryRetrieveLevel() == null) {
          it.setQueryRetrieveLevel("STUDY");
        }
      });
      createUI(queries);

    }
    /*
     * Iterable<CSVRecord> records =
     * CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in); for (CSVRecord
     * record : records) { logger.info(record.toString());
     * 
     * Query q = new Query(); if (record.isSet("PatientId")) {
     * q.setPatientId(record.get("PatientId")); } if
     * (record.isSet("PatientName")) {
     * q.setPatientName(record.get("PatientName")); } if
     * (record.isSet("AccessionNumber")) {
     * q.setAccessionNumber(record.get("AccessionNumber")); } if
     * (record.isSet("StudyDate")) { q.setStudyDate(new
     * Date(f.parse(record.get("StudyDate")).getTime())); } queries.add(q); } }
     * catch (IOException e) { logger.error("Error reading", e);
     * Notification.show("Error reading CSV file, see RCF4180 for details",
     * Notification.Type.ERROR_MESSAGE); } catch (ParseException e) {
     * Notification.show("Error parsing date: " + e.toString(),
     * Notification.Type.ERROR_MESSAGE); }
     */ catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void createUI(List<Query> queries) {
    Window window = new Window("Review CSV");
    window.setClosable(true);
    Grid<Query> grid = new Grid<>(Query.class);
    grid.setDataProvider(new ListDataProvider<>(queries));
    grid.removeColumn("queryId");
    grid.removeColumn("queryAttributes");
    grid.setColumnOrder("patientId", "patientName", "accessionNumber", "studyDate", "status", "message");
    grid.setWidth("100%");
    Button save = new Button("Save", event -> {
      queries.forEach(it -> {
        Main.queryDAO.createQuery(it);
      });
      Notification.show("Saved", Notification.Type.TRAY_NOTIFICATION);
      window.close();
    });

    Button close = new Button("Cancel", event -> window.close());
    VerticalLayout layout = new VerticalLayout();
    layout.addComponentsAndExpand(grid);
    layout.addComponent(new HorizontalLayout(save, close));
    window.setContent(layout);
    window.center();
    window.setWidth("600px");
    window.setHeight("800px");
    window.setModal(true);
    ui.addWindow(window);
  }

}
