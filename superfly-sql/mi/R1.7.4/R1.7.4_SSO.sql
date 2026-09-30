create table if not exists event_types (
  event_type_id        int auto_increment,
  event_code           varchar(24) not null,
  event_name           varchar(64) not null,
  primary key pk_event_types(event_type_id)
) engine = innodb;

create table if not exists events (
  event_id             bigint auto_increment,
  event_time           datetime not null,
  event_type_id        int,
  event_data           varchar(128) not null,
  subsystem_id         int null,
  index idx_events_event_time (event_time),
  index idx_events_subsystem_id (subsystem_id),
  primary key pk_events(event_id),
  constraint fk_event_types foreign key (event_type_id) references event_types (event_type_id),
  constraint fk_events_subsystem foreign key (subsystem_id) references subsystems (ssys_id)
) engine = innodb;

-- events created by the released R1.7.4 has no subsystem_id
call run_install_command('alter table events add column subsystem_id int null', '42S21');
call run_install_command('alter table events add index idx_events_subsystem_id (subsystem_id)', '42000');
-- a duplicate foreign key name raises an error that run_install_command does not ignore
set @fk_ddl = (select if(count(*) = 0,
                         'alter table events add constraint fk_events_subsystem foreign key (subsystem_id) references subsystems (ssys_id)',
                         'select 1')
                 from information_schema.table_constraints
                where constraint_schema = database()
                  and table_name = 'events'
                  and constraint_name = 'fk_events_subsystem');
prepare fk_stmt from @fk_ddl;
execute fk_stmt;
deallocate prepare fk_stmt;

commit;
