create table query (
  queryId integer primary key autoincrement,
  patientName varchar(128),
  patientId varchar(512),
  accessionNumber varchar(512),
  studyDate varchar(128),
  queryRetrieveLevel varchar(512) default 'STUDY',
  studyInstanceUID varchar(512),
  status varchar(32),
  message varchar(512)
);

create table move (
  moveId integer primary key autoincrement,
  queryId integer not null,
  studyInstanceUID varchar(512),
  seriesInstanceUID varchar(512),
  studyDescription varchar(512),
  seriesDescription varchar(512),
  patientName varchar(128),
  patientId varchar(512),
  accessionNumber varchar(512),
  queryRetrieveLevel varchar(512),
  status varchar(32),
  message varchar(512)
);
