create table job (
  job_id integer not null generated always as identity (increment by 1),
  name varchar(512),
  called_ae_title varchar(16),
  called_port int,
  calling_ae_title varchar(16),
  destination_ae_title varchar(16),
  hostname varchar(128),
  fetch_by varchar(64),
  queries_per_second int,
  concurrent_queries int,
  moves_per_second int,
  concurrent_moves int
);

create table query (
  query_id integer not null generated always as identity (increment by 1),
  job_id integer not null,
  patient_name varchar(128),
  patient_id varchar(512),
  accession_number varchar(512),
  study_date date,
  status varchar(32),
  message varchar(512)
);

create table move (
  move_id integer not null generated always as identity (increment by 1),
  job_id integer not null,
  query_id integer not null,
  study_instance_uid varchar(512),
  series_instance_uid varchar(512),
  status varchar(32),
  message varchar(512)
);
