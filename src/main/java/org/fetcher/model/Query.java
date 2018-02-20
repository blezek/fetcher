package org.fetcher.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.fetcher.State;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "query")
public class Query implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "query_id")
  @JsonProperty("query_id")
  private long id;

  @Column(name = "patient_name")
  @JsonProperty("patient_name")
  private String patientName;

  @Column(name = "patient_id")
  @JsonProperty("patient_id")
  private String patientId;

  @Column(name = "accession_number")
  @JsonProperty("accession_number")
  private String accessionNumber;

  @Column(name = "study_date")
  @JsonProperty("study_date")
  private Date studyDate;

  @Column(name = "status")
  @JsonProperty("status")
  private String status;

  @Column(name = "message")
  @JsonProperty("message")
  private String message;

  @ManyToOne
  @JoinColumn(name = "job_id", nullable = false)
  @JsonIgnore
  private Job job;

  @JsonIgnore
  public Map<String, String> getQueryAttributes() {
    HashMap<String, String> map = new HashMap<>();
    // Attributes
    if (patientName != null) {
      map.put("PatientName", patientName);
    }
    if (patientId != null) {
      map.put("PatienId", patientId);
    }
    if (accessionNumber != null) {
      map.put("AccessionNumber", accessionNumber);
    }
    if (studyDate != null) {
      map.put("StudyDate", new SimpleDateFormat("yyyyMMdd").format(studyDate));
    }
    return map;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public void setStatus(State state) {
    this.status = state.toString();
  }

  public void setJob(Job job) {
    this.job = job;
  }

}
