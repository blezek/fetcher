package org.fetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.fetcher.State;

import java.io.Serializable;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Query implements Serializable {
  private static final long serialVersionUID = 1L;
  @JsonIgnore
  static SimpleDateFormat f = new SimpleDateFormat("YYYYMMDD");

  public long queryId;
  public String patientName;
  public String patientId;
  public String accessionNumber;
  public String studyDate;
  private String queryRetrieveLevel = "STUDY";
  public String status = State.CREATED.toString();;
  public String message;

  @JsonIgnore
  public Map<String, String> getQueryAttributes() {
    HashMap<String, String> map = new HashMap<>();
    // Attributes
    if (patientName != null && !patientName.isEmpty()) {
      map.put("PatientName", patientName);
    }
    if (patientId != null && !patientId.isEmpty()) {
      map.put("PatientID", patientId);
    }
    if (accessionNumber != null && !accessionNumber.isEmpty()) {
      map.put("AccessionNumber", accessionNumber);
    }
    if (studyDate != null) {
      map.put("StudyDate", studyDate);
    }
    return map;
  }

  public long getQueryId() {
    return queryId;
  }

  public void setQueryId(long queryId) {
    this.queryId = queryId;
  }

  public String getPatientName() {
    return patientName;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getAccessionNumber() {
    return accessionNumber;
  }

  public void setAccessionNumber(String accessionNumber) {
    this.accessionNumber = accessionNumber;
  }

  public String getStudyDate() {
    return studyDate;
  }

  public void setStudyDate(String sd) throws ParseException {
    this.studyDate = sd;
  }
  //
  // public void setStudyDate(Date studyDate) {
  // this.studyDate = studyDate;
  // }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getQueryRetrieveLevel() {
    return queryRetrieveLevel;
  }

  public void setQueryRetrieveLevel(String queryRetrieveLevel) {
    this.queryRetrieveLevel = queryRetrieveLevel;
  }

  public void setStudyDateAsDate(Date studyDate) {
    this.studyDate = f.format(studyDate);
  }

  @JsonIgnore
  public Optional<java.util.Date> getStudyDateAsDate() {
    Optional<java.util.Date> dt = Optional.empty();
    try {
      dt = Optional.of(f.parse(this.studyDate));
    } catch (Exception e) {
    }
    return dt;
  }
}
