package org.fetcher.ui;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.StreamVariable;
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
import com.vaadin.ui.dnd.FileDropHandler;
import com.vaadin.ui.dnd.event.FileDropEvent;

import org.fetcher.Main;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class UploadCSV implements Receiver, SucceededListener, FileDropHandler<VerticalLayout> {
  static Logger logger = LoggerFactory.getLogger(UploadCSV.class);
  ByteArrayOutputStream buffer;
  UI ui;

  public UploadCSV(UI ui) {
    this.ui = ui;
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

  @Override
  public void drop(FileDropEvent<VerticalLayout> event) {
    // TODO Auto-generated method stub
    event.getFiles().forEach(file -> {
      file.setStreamVariable(new StreamVariable() {
        ByteArrayOutputStream fileBuffer = new ByteArrayOutputStream();

        // Output stream to write the file to
        @Override
        public OutputStream getOutputStream() {
          return fileBuffer;
        }

        @Override
        public boolean isInterrupted() {
          // TODO Auto-generated method stub
          return false;
        }

        // Returns whether onProgress() is called during upload
        @Override
        public boolean listenProgress() {
          return false;
        }

        // Called periodically during upload
        @Override
        public void onProgress(StreamingProgressEvent event) {
          Broadcaster.broadcast("Progress " + event.getBytesReceived() / file.getFileSize() * 100 + "%");
        }

        // Called when upload failed
        @Override
        public void streamingFailed(StreamingErrorEvent event) {
          Notification.show("CVS upload failed, fileName=" + event.getFileName(), Notification.Type.ERROR_MESSAGE);
        }

        // Called when upload finished
        @Override
        public void streamingFinished(StreamingEndEvent event) {
          ui.access(() -> {
            List<Query> queries = loadQueries(new ByteArrayInputStream(fileBuffer.toByteArray()));
            createUI(queries);
          });
        }

        // Called when upload started
        @Override
        public void streamingStarted(StreamingStartEvent event) {
          Notification.show("Upload started " + event.getFileName());
        }
      });
    });
  }

  private List<Query> loadQueries(InputStream out) {
    List<Query> queries = new ArrayList<>();
    try (InputStreamReader in = new InputStreamReader(out)) {
      // CSVReader csvReader = new
      // CSVReaderBuilder(in).withSkipLines(1).build();
      // ColumnPositionMappingStrategy<Query> strategy = new
      // ColumnPositionMappingStrategy<>();
      HeaderColumnNameMappingStrategy<Query> strategy = new HeaderColumnNameMappingStrategy<>();
      strategy.setType(Query.class);
      String[] memberFieldsToBindTo = { "patientId", "patientName", "accessionNumber", "studyDate",
          "queryRetrieveLevel" };
      // strategy.setColumnMapping(memberFieldsToBindTo);
      CsvToBean<Query> csvToBean = new CsvToBeanBuilder<Query>(in).withMappingStrategy(strategy).withSkipLines(0)
          .withIgnoreLeadingWhiteSpace(true).build();
      queries = csvToBean.parse();
      queries.forEach(it -> {
        if (it.getQueryRetrieveLevel() == null) {
          it.setQueryRetrieveLevel("STUDY");
        }
      });
    } catch (IOException e) {
      logger.error("Error uploading files", e);
    }
    return queries;

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
    // Load the CSV
    ByteArrayInputStream out = new ByteArrayInputStream(buffer.toByteArray());
    createUI(loadQueries(out));
  }

}
