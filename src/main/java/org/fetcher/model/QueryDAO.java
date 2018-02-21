package org.fetcher.model;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.List;

public interface QueryDAO {

  @SqlUpdate("insert into query ( patientName, patientId, accessionNumber, studyDate, status, message ) values ( :q.patientName, :q.patientId, :q.accessionNumber, :q.studyDate, :q.status, :q.message )")
  @GetGeneratedKeys
  int createQuery(@BindBean("q") Query q);

  @SqlUpdate("update query set patientName = :q.patientName, patientId = :q.patientId, accessionNumber = :q.accessionNumber, studyDate = :q.studyDate, status = :q.status, message = :q.message where queryId = :q.queryId")
  public void update(@BindBean("q") Query query);

  @SqlQuery("select * from query")
  @MapResultAsBean
  List<Query> getQueries();

  @SqlQuery("select * from move where queryId = :id")
  @MapResultAsBean
  List<Move> getMoves(@Bind("id") int queryId);

  @SqlUpdate("insert into move (queryId, studyInstanceUID, seriesInstanceUID, numberOfSeriesRelatedInstances, status, message ) values ( :m.queryId, :m.studyInstanceUID, :m.seriesInstanceUID, :m.numberOfSeriesRelatedInstances, :m.status, :m.message )")
  @GetGeneratedKeys
  int createMove(@BindBean("m") Move m);

  @SqlQuery("select * from move where moveId = :id")
  @MapResultAsBean
  Move getMove(@Bind("id") int id);

}
