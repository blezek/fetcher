package org.fetcher.ui;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.fetcher.Main;
import org.fetcher.State;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@SuppressWarnings("serial")
public class AddQueryDialog extends Window {
  static Logger logger = LoggerFactory.getLogger(AddQueryDialog.class);
  Query query = null;
  Binder<Query> binder = new Binder<>(Query.class);
  boolean isEdit = false;
  RefreshListener listener;

  public AddQueryDialog(Query query, RefreshListener listener) {
    super("Edit Query"); // Set window caption center();
    this.query = query;
    setup(listener);
    isEdit = true;
    binder.readBean(query);
  }

  public AddQueryDialog(RefreshListener listener) {
    super("Add query");
    setup(listener);
    query = new Query();
  }

  void setup(RefreshListener listener) {
    this.listener = listener;
    // Disable the close button
    setClosable(true);

    FormLayout layout = new FormLayout();
    TextField patientName = new TextField("Patient Name");
    binder.bind(patientName, Query::getPatientName, Query::setPatientName);
    TextField patientId = new TextField("Patient Id");
    binder.bind(patientId, Query::getPatientId, Query::setPatientId);

    TextField accessionNumber = new TextField("Accession Number");
    binder.bind(accessionNumber, Query::getAccessionNumber, Query::setAccessionNumber);

    DateField studyDate = new DateField("Study Date(YYYYMMDD)");
    studyDate.setDateFormat("YYYYMMDD");
    if (query != null && query.getStudyDateAsDate().isPresent()) {
      Optional<java.util.Date> dt = query.getStudyDateAsDate();
      LocalDateTime ldt = LocalDateTime.ofInstant(dt.get().toInstant(), ZoneId.systemDefault());
      studyDate.setDefaultValue(ldt.toLocalDate());
    } else {
      studyDate.setDefaultValue(null);
    }

    // Create the selection component
    ComboBox<String> select = new ComboBox<>("Query Retrieve Level");

    // Add some items
    select.setItems("PATIENT", "STUDY", "SERIES");
    binder.bind(select, "queryRetrieveLevel");

    CheckBox addQueued = new CheckBox("Queued?");
    addQueued.setValue(true);
    layout.addComponents(patientName, patientId, accessionNumber, studyDate, select, addQueued);
    Button save = new Button("Save", event -> {
      try {
        binder.writeBean(query);
      } catch (ValidationException e) {
        logger.error("error getting bean values", e);
      }
      if (studyDate.getValue() != studyDate.getEmptyValue()) {
        query.setStudyDateAsDate(Date.valueOf(studyDate.getValue()));
      }
      if (addQueued.getValue()) {
        query.setStatus(State.QUEUED.toString());
      }
      if (isEdit) {
        Main.queryDAO.update(query);
      } else {
        Main.queryDAO.createQuery(query);
      }
      close();
      if (listener != null) {
        listener.refresh();
      }
    });
    Button close = new Button("Cancel", event -> close());

    setContent(new VerticalLayout(layout, new HorizontalLayout(save, close)));
    setModal(true);
    setWidth("600px");
  }

}
