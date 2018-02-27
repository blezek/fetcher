package org.fetcher.ui;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import org.fetcher.Fetcher;
import org.fetcher.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Title("fetcher")
@Push
public class FetcherUI extends UI implements Broadcaster.BroadcastListener {
  private static final long serialVersionUID = 1L;
  static Logger logger = LoggerFactory.getLogger(FetcherUI.class);

  private Label activityTicker = new Label("Status message");
  Label queryStatus = new Label("Status Message");
  Label moveStatus = new Label("Status Message");
  Fetcher fetcher = null;

  @Override
  protected void init(VaadinRequest request) {
    fetcher = Main.fetcher;
    // Create the content root layout for the UI
    VerticalLayout content = new VerticalLayout();
    content.setSizeFull(); // Use entire window

    setContent(content);

    // Display the greeting
    content.addComponent(new Label("Fetcher"));

    GridLayout layout = new GridLayout(2, 2);
    layout.setSpacing(true);
    layout.addComponent(new Label("Called AE:"));
    layout.addComponent(new Label(Main.fetcher.getCalledAET() + "@" + Main.fetcher.getHostname() + ":" + Integer.toString(Main.fetcher.getCalledPort())));
    layout.addComponent(new Label("Calling AE Title"));
    layout.addComponent(new Label(Main.fetcher.getCallingAET()));
    layout.addComponent(new Label("Destination AE Title"));
    layout.addComponent(new Label(Main.fetcher.getDestinationAET()));
    content.addComponent(layout);
    content.addComponents(queryStatus, moveStatus);

    layout = new GridLayout(4, 4);
    layout.setSpacing(true);
    Button startQuery = new Button("Start");
    startQuery.addClickListener(click -> {
      if (!fetcher.isQueryRunning()) {
        startQuery.setCaption("Start");
        fetcher.startFind();
        Notification.show("stopping find");
      } else {
        fetcher.stopFind();
        startQuery.setCaption("Start");
        Notification.show("stopping find");
      }
    });
    layout.addComponent(new Label("Query"));
    layout.addComponent(startQuery);
    layout.addComponent(new Label(Main.fetcher.getConcurrentQueries() + " concurrent queries / " + Main.fetcher.queriesPerSecond + " queries per second limit"));
    layout.newLine();
    Button startMove = new Button("Start");
    startMove.addClickListener(event -> {
      if (!fetcher.isMoveRunning()) {
        startMove.setCaption("Stop");
        fetcher.startMove();
        Notification.show("starting move");
      } else {
        fetcher.stopMove();
        startMove.setCaption("Start");
        Notification.show("stopping move");
      }
    });
    layout.addComponent(new Label("Move:"));
    layout.addComponent(startMove);
    layout.addComponent(new Label(Main.fetcher.getConcurrentMoves() + " concurrent moves / " + Main.fetcher.imagesPerSecond + " images per second limit"));
    content.addComponent(layout);

    content.addComponentsAndExpand(new QueryGrid(this));
    // Have a clickable button
    // content.addComponent(new Button("Push Me!", click ->
    // Notification.show("Pushed!")));

    // ProgressBar bar = new ProgressBar(0.0f);
    // content.addComponent(bar);
    // Panel spacer = new Panel();
    // content.addComponentsAndExpand(new VerticalLayout());
    Broadcaster.register(this);
    content.addComponent(activityTicker);
    update();
  }

  void update() {
    moveStatus.setValue("Move: " + fetcher.movePool.getActiveCount() + " active threads / " + fetcher.moveQueue.size() + " pending jobs / " + fetcher.imagesPerMinute.size() + " images per minute");
    queryStatus.setValue("Query: " + fetcher.queryPool.getActiveCount() + " active threads / " + fetcher.queryQueue.size() + " pending jobs");
  }

  @Override
  public void receiveBroadcast(String message) {
    access(() -> {
      activityTicker.setValue(message);
      update();
    });

  }

  @Override
  public void detach() {
    Broadcaster.unregister(this);
    super.detach();
  }
}
