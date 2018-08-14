package org.fetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class Move {
  private Long moveId;
  private Long queryId;
  private String studyInstanceUID;
  private String seriesInstanceUID;
  private String studyDescription;
  private String seriesDescription;
  public String patientName;
  public String patientId;
  public String accessionNumber;
  private String queryRetrieveLevel;
  private String status;
  private String message;

  public String getAccessionNumber() {
    return accessionNumber;
  }

  public String getMessage() {
    return message;
  }

  @JsonIgnore
  public Map<String, String> getMoveAttributes() {
    HashMap<String, String> map = new HashMap<>();
    // Attributes
    if (studyInstanceUID != null) {
      map.put("StudyInstanceUID", studyInstanceUID);
    }
    if (seriesInstanceUID != null) {
      map.put("SeriesInstanceUID", seriesInstanceUID);
    }
    return map;
  }

  public Long getMoveId() {
    return moveId;
  }

  public String getPatientId() {
    return patientId;
  }

  public String getPatientName() {
    return patientName;
  }

  public Long getQueryId() {
    return queryId;
  }

  public String getQueryRetrieveLevel() {
    return queryRetrieveLevel;
  }

  public String getSeriesInstanceUID() {
    return seriesInstanceUID;
  }

  public String getStatus() {
    return status;
  }

  public String getStudyInstanceUID() {
    return studyInstanceUID;
  }

  public void setAccessionNumber(String accessionNumber) {
    this.accessionNumber = accessionNumber;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setMoveId(Long moveId) {
    this.moveId = moveId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public void setQueryId(Long queryId) {
    this.queryId = queryId;
  }

  public void setQueryRetrieveLevel(String queryRetrieveLevel) {
    this.queryRetrieveLevel = queryRetrieveLevel;
  }

  public void setSeriesInstanceUID(String seriesInstanceUID) {
    this.seriesInstanceUID = seriesInstanceUID;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setStudyInstanceUID(String studyInstanceUID) {
    this.studyInstanceUID = studyInstanceUID;
  }

  public String getStudyDescription() {
    return studyDescription;
  }

  public void setStudyDescription(String studyDescription) {
    this.studyDescription = studyDescription;
  }

  public String getSeriesDescription() {
    return seriesDescription;
  }

  public void setSeriesDescription(String seriesDescription) {
    this.seriesDescription = seriesDescription;
  }

}
