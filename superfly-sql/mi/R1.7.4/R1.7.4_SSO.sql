drop table if exists event_types;

create table event_types (
  event_type_id        int auto_increment,
  event_code           varchar(24) not null,
  event_name           varchar(64) not null,
  primary key pk_event_types(event_type_id)
) engine = innodb;

drop table if exists events;

create table events (
  event_id             bigint auto_increment,
  event_time           datetime not null,
  event_type_id        int,
  event_data           varchar(128) not null,
  primary key pk_events(event_id),
  constraint fk_event_types foreign key (event_type_id) references event_types (event_type_id)
) engine = innodb;

commit;
