package org.fetcher.model;

import org.fetcher.State;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.List;

public interface QueryDAO {

  @SqlUpdate("insert into move (queryId, studyInstanceUID, seriesInstanceUID, studyDescription, seriesDescription, patientName, patientId, accessionNumber, queryRetrieveLevel, status, message ) values ( :m.queryId, :m.studyInstanceUID, :m.seriesInstanceUID, :m.studyDescription, :m.seriesDescription, :m.patientName, :m.patientId, :m.accessionNumber, :m.queryRetrieveLevel, :m.status, :m.message )")
  @GetGeneratedKeys
  int createMove(@BindBean("m") Move m);

  @SqlUpdate("insert into query ( patientName, patientId, accessionNumber, studyDate, queryRetrieveLevel, studyInstanceUID, status, message ) values ( :q.patientName, :q.patientId, :q.accessionNumber, :q.studyDate, :q.queryRetrieveLevel, :q.studyInstanceUID, :q.status, :q.message )")
  @GetGeneratedKeys
  int createQuery(@BindBean("q") Query q);

  @SqlUpdate("delete from query where queryId = :id")
  void delete(@Bind("id") long l);

  @SqlQuery("select * from move where moveId = :id")
  @MapResultAsBean
  Move getMove(@Bind("id") int id);

  @SqlQuery("select * from move where queryId = :id")
  @MapResultAsBean
  List<Move> getMoves(@Bind("id") int queryId);

  @SqlQuery("select * from query")
  @MapResultAsBean
  List<Query> getQueries();

  @SqlQuery("select * from query where status = :status")
  @MapResultAsBean
  List<Query> getQueries(@Bind("status") State status);

  @SqlQuery("select count(*) from move where status = :status")
  int moveCount(@Bind("status") State status);

  @SqlQuery("select count(*) from query")
  int queryCount();

  @SqlQuery("select count(*) from query where status = :status")
  int queryCount(@Bind("status") State status);

  @SqlUpdate("update query set patientName = :q.patientName, patientId = :q.patientId, accessionNumber = :q.accessionNumber, studyDate = :q.studyDate, status = :q.status, message = :q.message, queryRetrieveLevel = :q.queryRetrieveLevel, studyInstanceUID = :q.studyInstanceUID where queryId = :q.queryId")
  public void update(@BindBean("q") Query query);

  @SqlQuery("select * from move")
  @MapResultAsBean
  List<Move> getMoves();

}
