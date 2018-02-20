package org.fetcher.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "move")
public class Move {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "move_id")
  private long moveId;

  @Column(name = "query_id", insertable = false, updatable = false)
  private String queryId;
  @Column(name = "study_instance_uid")
  private String studyInstanceUid;
  @Column(name = "series_instance_uid")
  private String seriesInstanceUid;
  @Column(name = "status")
  private String status;
  @Column(name = "message")
  private String message;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "query_id")
  private Query query;

}
