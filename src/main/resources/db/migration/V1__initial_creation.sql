create table query (
  queryId integer not null generated always as identity (increment by 1),
  patientName varchar(128),
  patientId varchar(512),
  accessionNumber varchar(512),
  studyDate date,
  status varchar(32),
  message varchar(512)
);

create table move (
  moveId integer not null generated always as identity (increment by 1),
  queryId integer not null,
  studyInstanceUID varchar(512),
  seriesInstanceUID varchar(512),
  numberOfSeriesRelatedInstances int default 1,
  status varchar(32),
  message varchar(512)
);
