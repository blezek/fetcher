package org.fetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;

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
  static SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");

  @CsvBindByName
  public long queryId;
  @CsvBindByName
  public String patientName;
  @CsvBindByName
  public String patientId;
  @CsvBindByName
  public String accessionNumber;
  @CsvBindByName
  public String studyDate;
  @CsvBindByName
  private String queryRetrieveLevel = "STUDY";

  public String status = State.CREATED.toString();;
  public String message;
  private String studyInstanceUID;

  public String getAccessionNumber() {
    return accessionNumber;
  }

  public String getMessage() {
    return message;
  }

  public String getPatientId() {
    return patientId;
  }

  public String getPatientName() {
    return patientName;
  }

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
    if (studyInstanceUID != null) {
      map.put("StudyInstanceUID", studyInstanceUID);
    }
    return map;
  }

  public long getQueryId() {
    return queryId;
  }

  public String getQueryRetrieveLevel() {
    return queryRetrieveLevel;
  }

  public String getStatus() {
    return status;
  }

  public String getStudyDate() {
    return studyDate;
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

  public void setAccessionNumber(String accessionNumber) {
    this.accessionNumber = accessionNumber;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public void setQueryId(long queryId) {
    this.queryId = queryId;
  }

  public void setQueryRetrieveLevel(String queryRetrieveLevel) {
    this.queryRetrieveLevel = queryRetrieveLevel;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setStudyDate(String sd) throws ParseException {
    this.studyDate = sd;
  }
  //
  // public void setStudyDate(Date studyDate) {
  // this.studyDate = studyDate;
  // }

  public void setStudyDateAsDate(Date studyDate) {
    this.studyDate = f.format(studyDate);
  }

  public String getStudyInstanceUID() {
    return studyInstanceUID;
  }

  public void setStudyInstanceUID(String studyInstanceUID) {
    this.studyInstanceUID = studyInstanceUID;
  }
}
