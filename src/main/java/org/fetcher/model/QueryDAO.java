package org.fetcher.model;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.List;

public interface QueryDAO {

  @SqlQuery("select * from query where jobId = ?")
  @MapResultAsBean
  public List<Query> findAllByJobId(int jobId);

  @SqlUpdate("insert into query ( jobId, patientName, patientId, accessionNumber, studyDate, status, message ) values ( :job.jobId, :q.patientName, :q.patientId, :q.accessionNumber, :q.studyDate, :q.status, :q.message )")
  @GetGeneratedKeys
  int createQuery(@BindBean("job") Job job, @BindBean("q") Query q);

  @SqlUpdate("update query set patientName = :q.patientName, patientId = :q.patientId, accessionNumber = :q.accessionNumber, studyDate = :q.studyDate, status = :q.status, message = :q.message where queryId = :q.queryId")
  public void update(@BindBean("q") Query query);

}
