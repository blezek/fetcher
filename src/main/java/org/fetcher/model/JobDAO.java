package org.fetcher.model;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.List;

public interface JobDAO {
  @SqlQuery("select * from query where jobId = :job.jobId")
  @MapResultAsBean
  List<Query> getQueries(@BindBean("job") Job job);

  @SqlUpdate("insert into job ( name, calledAET, calledPort, callingAET, destinationAET, hostname, fetchBy, queriesPerSecond, concurrentQueries, movesPerSecond, concurrentMoves ) values ( :job.name, :job.calledAET, :job.calledPort, :job.callingAET, :job.destinationAET, :job.hostname, :job.fetchBy, :job.queriesPerSecond, :job.concurrentQueries, :job.movesPerSecond, :job.concurrentMoves )")
  @GetGeneratedKeys
  int save(@BindBean("job") Job job);

  @SqlQuery("select * from job")
  @MapResultAsBean
  List<Job> findAll();

}
