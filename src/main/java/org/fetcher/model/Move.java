package org.fetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class Move {
  private Long moveId;
  private Long queryId;
  private String studyInstanceUID;
  private String seriesInstanceUID;
  private Integer numberOfSeriesRelatedInstances;
  private String status;
  private String message;

  public Long getMoveId() {
    return moveId;
  }

  public void setMoveId(Long moveId) {
    this.moveId = moveId;
  }

  public Long getQueryId() {
    return queryId;
  }

  public void setQueryId(Long queryId) {
    this.queryId = queryId;
  }

  public String getStudyInstanceUID() {
    return studyInstanceUID;
  }

  public void setStudyInstanceUID(String studyInstanceUID) {
    this.studyInstanceUID = studyInstanceUID;
  }

  public String getSeriesInstanceUID() {
    return seriesInstanceUID;
  }

  public void setSeriesInstanceUID(String seriesInstanceUID) {
    this.seriesInstanceUID = seriesInstanceUID;
  }

  public Integer getNumberOfSeriesRelatedInstances() {
    return numberOfSeriesRelatedInstances;
  }

  public void setNumberOfSeriesRelatedInstances(Integer numberOfSeriesRelatedInstances) {
    this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
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

}
