package org.fetcher.ui;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import org.fetcher.Main;
import org.fetcher.model.Move;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("serial")
public class QueryGrid extends CustomComponent implements RefreshListener {
  static Logger logger = LoggerFactory.getLogger(QueryGrid.class);
  VerticalLayout layout = new VerticalLayout();
  Grid<Query> grid = new Grid<>(Query.class);
  Grid<Move> moves = new Grid<>(Move.class);

  public QueryGrid(UI ui) {

    // Buttons
    HorizontalLayout hLayout = new HorizontalLayout();
    Button queue = new Button("Queue Query", click -> {
      if (Main.fetcher.isQueryRunning()) {
        Notification.show("Stop Query before adding items to the queue", Notification.Type.ERROR_MESSAGE);
        return;
      }
      grid.getSelectedItems().forEach(c -> {
        Main.fetcher.queue(c.getQueryId());
      });
      Notification.show("queued " + grid.getSelectedItems().size() + " items");
      refresh();
    });
    Button addQuery = new Button("Add", click -> {
      if (Main.fetcher.isQueryRunning()) {
        Notification.show("Stop query before editing query", Notification.Type.ERROR_MESSAGE);
        return;
      }
      ui.addWindow(new AddQueryDialog(() -> refresh()));
    });
    Button editQuery = new Button("Edit", click -> {
      if (Main.fetcher.isQueryRunning()) {
        Notification.show("Stop query before editing query", Notification.Type.ERROR_MESSAGE);
        return;
      }
      grid.getSelectedItems().forEach(c -> {
        ui.addWindow(new AddQueryDialog(c, () -> refresh()));
      });
    });
    editQuery.setEnabled(false);
    Button queueAll = new Button("Queue All", click -> {
      if (Main.fetcher.isQueryRunning()) {
        Notification.show("Stop Query before adding items to the queue", Notification.Type.ERROR_MESSAGE);
        return;
      }
      Main.fetcher.queueAll();
      Notification.show("queued all");
      refresh();
    });
    Button deleteButton = new Button("Delete", click -> {
      if (Main.fetcher.isQueryRunning()) {
        Notification.show("Stop Query before deleting queries", Notification.Type.ERROR_MESSAGE);
        return;
      }
      grid.getSelectedItems().forEach(c -> {
        Main.queryDAO.delete(c.getQueryId());
      });
      Notification.show("deleted");
      refresh();
    });

    Button refresh = new Button("Refresh", click -> {
      refresh();
      Broadcaster.broadcast("");
    });

    hLayout.addComponents(queue, queueAll, addQuery, editQuery, deleteButton, refresh);
    layout.addComponent(hLayout);

    grid.setDataProvider((

        List<QuerySortOrder> sortOrder, int offset, int limit) -> {
      return Main.jdbi.withHandle((handle) -> {
        return handle.createQuery("select * from query offset " + offset + " rows fetch first " + limit + " rows only").map(Query.class).list().stream();
      });
    }, () -> {
      return Main.queryDAO.queryCount();
    });
    grid.removeColumn("queryId");
    grid.removeColumn("queryAttributes");
    grid.setColumnOrder("patientId", "patientName", "accessionNumber", "studyDate", "status", "message");
    grid.setWidth("100%");
    grid.getColumns().forEach((c) -> {
      logger.info("Query columns: " + c.getCaption() + " -- " + c.getId());
    });
    grid.setSelectionMode(SelectionMode.MULTI);
    grid.addSelectionListener(c -> {
      editQuery.setEnabled(c.getAllSelectedItems().size() == 1);
    });

    Button queueAllMoves = new Button("Queue All", click -> {
      if (Main.fetcher.isMoveRunning()) {
        Notification.show("Stop Move before adding items to the queue", Notification.Type.ERROR_MESSAGE);
        return;
      }
      Main.fetcher.queueAllMoves();
      Notification.show("queued all");
      refresh();
    });

    Button queueMoves = new Button("Queue Selected", click -> {
      if (Main.fetcher.isMoveRunning()) {
        Notification.show("Stop Move before adding items to the queue", Notification.Type.ERROR_MESSAGE);
        return;
      }
      moves.getSelectedItems().forEach(c -> {
        Main.fetcher.queueMove(c.getMoveId());
      });
      Notification.show("queued " + moves.getSelectedItems().size());
      refresh();
    });

    moves.setDataProvider((List<QuerySortOrder> sortOrder, int offset, int limit) -> {
      return Main.jdbi.withHandle((handle) -> {
        return handle.createQuery("select * from move offset " + offset + " rows fetch first " + limit + " rows only").map(Move.class).list().stream();
      });
    }, () -> {
      return Main.jdbi.withHandle((handle) -> {
        return handle.createQuery("select count(*) from move").mapTo(Integer.class).first();
      });
    });
    moves.removeColumn("moveAttributes");
    moves.removeColumn("moveId");
    moves.removeColumn("queryId");
    moves.setColumnOrder("patientId", "patientName", "accessionNumber", "status", "message");

    for (Column<Move, ?> c : moves.getColumns()) {
      logger.info("Move columns: " + c.getCaption() + " -- " + c.getId());
    }
    moves.setWidth("100%");
    moves.setSelectionMode(SelectionMode.MULTI);

    layout.addComponentsAndExpand(grid);
    layout.addComponent(new HorizontalLayout(queueAllMoves, queueMoves));
    layout.addComponentsAndExpand(moves);

    setCompositionRoot(layout);
  }

  @Override
  public void refresh() {
    grid.getDataProvider().refreshAll();
    moves.getDataProvider().refreshAll();
  }
}