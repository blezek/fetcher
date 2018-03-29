package org.fetcher.model;

import java.io.Serializable;

public class Job implements Serializable {

  private static final long serialVersionUID = 1L;
  public int jobId;
  public String name = "default";
  public String calledAET = "pacs";
  public int calledPort = 1234;
  public String hostname = "example.com";
  public String callingAET = "fetcher";
  public String destinationAET = "fetcher";
  public String fetchBy = "STUDY";
  public int queriesPerSecond = 2;
  public int concurrentQueries = 5;
  public int movesPerSecond = 2;
  public int concurrentMoves = 5;

  public String getCalledAET() {
    return calledAET;
  }

  public int getCalledPort() {
    return calledPort;
  }

  public String getCallingAET() {
    return callingAET;
  }

  public int getConcurrentMoves() {
    return concurrentMoves;
  }

  public int getConcurrentQueries() {
    return concurrentQueries;
  }

  public String getDestinationAET() {
    return destinationAET;
  }

  public String getFetchBy() {
    return fetchBy;
  }

  public String getHostname() {
    return hostname;
  }

  public int getJobId() {
    return jobId;
  }

  public int getMovesPerSecond() {
    return movesPerSecond;
  }

  public String getName() {
    return name;
  }

  public int getQueriesPerSecond() {
    return queriesPerSecond;
  }

  public void setCalledAET(String calledAET) {
    this.calledAET = calledAET;
  }

  public void setCalledPort(int calledPort) {
    this.calledPort = calledPort;
  }

  public void setCallingAET(String callingAET) {
    this.callingAET = callingAET;
  }

  public void setConcurrentMoves(int concurrentMoves) {
    this.concurrentMoves = concurrentMoves;
  }

  public void setConcurrentQueries(int concurrentQueries) {
    this.concurrentQueries = concurrentQueries;
  }

  public void setDestinationAET(String destinationAET) {
    this.destinationAET = destinationAET;
  }

  public void setFetchBy(String fetchBy) {
    this.fetchBy = fetchBy;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public void setJobId(int jobId) {
    this.jobId = jobId;
  }

  public void setMovesPerSecond(int movesPerSecond) {
    this.movesPerSecond = movesPerSecond;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setQueriesPerSecond(int queriesPerSecond) {
    this.queriesPerSecond = queriesPerSecond;
  }

}
