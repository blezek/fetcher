create table job (
  jobId integer not null generated always as identity (increment by 1),
  name varchar(512),
  calledAET varchar(16),
  calledPort int,
  callingAET varchar(16),
  destinationAET varchar(16),
  hostname varchar(128),
  fetchBy varchar(64),
  queriesPerSecond int,
  concurrentQueries int,
  movesPerSecond int,
  concurrentMoves int
);

create table query (
  queryId integer not null generated always as identity (increment by 1),
  jobId integer not null,
  patientName varchar(128),
  patientId varchar(512),
  accessionNumber varchar(512),
  studyDate date,
  status varchar(32),
  message varchar(512)
);

create table move (
  moveId integer not null generated always as identity (increment by 1),
  jobId integer not null,
  queryId integer not null,
  studyInstanceUID varchar(512),
  seriesInstanceUID varchar(512),
  status varchar(32),
  message varchar(512)
);
