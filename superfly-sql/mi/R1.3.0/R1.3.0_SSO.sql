drop table if exists smtp_servers;

create table smtp_servers(
  ssrv_id              int(10) auto_increment,
  server_name          varchar(32) not null,
  host                 varchar(64) not null,
  port                 int(10),
  username             varchar(64),
  password             varchar(64),
  from_address         varchar(64),
  unique unq_smtp_servers_server_name (server_name),
  primary key pk_smtp_servers(ssrv_id)
) engine = innodb;

call run_install_command('alter table subsystems add column ssrv_ssrv_id int(10)', '42S21');
call run_install_command('alter table subsystems add foreign key fk_subsystems_smtp_servers foreign key (ssrv_ssrv_id) references smtp_servers (ssrv_id)', '42S21');

commit;