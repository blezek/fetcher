package org.fetcher.ui;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;

import org.fetcher.Main;
import org.fetcher.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("serial")
public class MovesGrid extends CustomComponent {
  static Logger logger = LoggerFactory.getLogger(MovesGrid.class);

  public MovesGrid() {
    Grid<Move> moves = new Grid<>(Move.class);
    moves.setDataProvider((List<QuerySortOrder> sortOrder, int offset, int limit) -> {
      return Main.jdbi.withHandle((handle) -> {
        return handle.createQuery("select * from move offset " + offset + " rows fetch first " + limit + " rows only")
            .map(Move.class).list().stream();
      });
    }, () -> {
      return Main.jdbi.withHandle((handle) -> {
        return handle.createQuery("select count(*) from move").mapTo(Integer.class).first();
      });
    });
    moves.removeColumn("moveAttributes");
    moves.removeColumn("moveId");
    moves.removeColumn("queryId");
    for (Column<Move, ?> c : moves.getColumns()) {
      logger.info("Move columns: " + c.getCaption() + " -- " + c.getId());
    }
    moves.setWidth("100%");
    setCompositionRoot(moves);
  }
}
