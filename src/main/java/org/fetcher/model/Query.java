package org.fetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.fetcher.State;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Query implements Serializable {
  private static final long serialVersionUID = 1L;
  public long queryId;
  public String patientName;
  public String patientId;
  public String accessionNumber;
  public Date studyDate;
  private String queryRetrieveLevel = "STUDY";
  public String status = State.CREATED.toString();;
  public String message;

  @JsonIgnore
  public Map<String, String> getQueryAttributes() {
    HashMap<String, String> map = new HashMap<>();
    // Attributes
    if (patientName != null) {
      map.put("PatientName", patientName);
    }
    if (patientId != null) {
      map.put("PatientID", patientId);
    }
    if (accessionNumber != null) {
      map.put("AccessionNumber", accessionNumber);
    }
    if (studyDate != null) {
      map.put("StudyDate", new SimpleDateFormat("yyyyMMdd").format(studyDate));
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

  public Date getStudyDate() {
    return studyDate;
  }

  public void setStudyDate(Date studyDate) {
    this.studyDate = studyDate;
  }

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
}
