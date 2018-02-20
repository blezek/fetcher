package org.fetcher.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "job")
public class Job implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "job_id")
  int jobId;

  @Column(name = "name")
  @JsonProperty("name")
  String name = "default";

  @Column(name = "called_ae_title")
  @JsonProperty("called_ae_title")
  String called = "pacs";

  @Column(name = "called_port")
  @JsonProperty("called_port")
  int calledPort = 1234;

  @Column(name = "hostname")
  @JsonProperty("hostname")
  String hostname = "example.com";

  @Column(name = "calling_ae_title")
  @JsonProperty("calling_ae_title")
  String calling = "fetcher";

  @Column(name = "destination_ae_title")
  @JsonProperty("destination_ae_title")
  String destination = "fetcher";

  @Column(name = "fetch_by")
  @JsonProperty("fetch_by")
  String fetchBy = "STUDY";

  @Column(name = "queries_per_second")
  @JsonProperty("queries_per_second")
  int queriesPerSecond = 2;

  @Column(name = "concurrent_queries")
  @JsonProperty("concurrent_queries")
  int concurrentQueries = 5;

  @Column(name = "moves_per_second")
  @JsonProperty("moves_per_second")
  int movesPerSecond = 2;

  @Column(name = "concurrent_moves")
  @JsonProperty("concurrent_moves")
  int concurrentMoves = 5;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "job", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Query> queries;

  public int getJobId() {
    return jobId;
  }

  public void setJobId(int jobId) {
    this.jobId = jobId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCalled() {
    return called;
  }

  public void setCalled(String called) {
    this.called = called;
  }

  public int getCalledPort() {
    return calledPort;
  }

  public void setCalledPort(int calledPort) {
    this.calledPort = calledPort;
  }

  public String getCalling() {
    return calling;
  }

  public void setCalling(String calling) {
    this.calling = calling;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getFetchBy() {
    return fetchBy;
  }

  public void setFetchBy(String fetchBy) {
    this.fetchBy = fetchBy;
  }

  public int getQueriesPerSecond() {
    return queriesPerSecond;
  }

  public void setQueriesPerSecond(int queriesPerSecond) {
    this.queriesPerSecond = queriesPerSecond;
  }

  public int getConcurrentQueries() {
    return concurrentQueries;
  }

  public void setConcurrentQueries(int concurrentQueries) {
    this.concurrentQueries = concurrentQueries;
  }

  public int getMovesPerSecond() {
    return movesPerSecond;
  }

  public void setMovesPerSecond(int movesPerSecond) {
    this.movesPerSecond = movesPerSecond;
  }

  public int getConcurrentMoves() {
    return concurrentMoves;
  }

  public void setConcurrentMoves(int concurrentMoves) {
    this.concurrentMoves = concurrentMoves;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Set<Query> getQueries() {
    return queries;
  }

}
