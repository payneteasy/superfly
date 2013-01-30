drop table if exists subsystem_tokens;
drop table if exists sso_sessions;

create table sso_sessions(
  ssos_id              int(10) auto_increment,
  identifier           varchar(64) not null,
  user_user_id         int(10) not null,
  created_date         datetime not null,
  access_date          datetime not null,
  primary key pk_sso_sessions(ssos_id),
  unique unq_sso_sessions_identifier (identifier),
  index idx_sso_sessions_user_user_id (user_user_id),
  index idx_sso_sessions_access_date (access_date),
  constraint fk_sso_sessions_users foreign key (user_user_id) references users (user_id)
) engine = innodb;

create table subsystem_tokens(
  stok_id              int(10) auto_increment,
  token                varchar(64) not null,
  ssos_ssos_id         int(10) not null,
  ssys_ssys_id         int(10) not null,
  created_date         datetime not null,
  primary key pk_subsystem_tokens(stok_id),
  unique unq_subsystem_tokens_token (token),
  index idx_subsystem_tokens_ssos_ssos_id (ssos_ssos_id),
  index idx_subsystem_tokens_ssys_ssys_id (ssys_ssys_id),
  index idx_subsystem_tokens_created_date (created_date),
  constraint fk_subsystem_tokens_sso_sessions foreign key (ssos_ssos_id) references sso_sessions (ssos_id),
  constraint fk_subsystem_tokens_subsystem foreign key (ssys_ssys_id) references subsystems (ssys_id)
) engine = innodb;

call run_install_command('alter table subsystems add column landing_url varchar(255) not null', '42S21');

commit;