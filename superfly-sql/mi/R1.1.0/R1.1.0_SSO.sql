call run_install_command('alter table subsystems add column allow_list_users char(1) not null default "N"', '42S21');

drop table if exists sessions;
create table sessions(
  sess_id              int(10) auto_increment,
  start_date           datetime,
  user_user_id         int(10),
  ssys_ssys_id         int(10),
  callback_information varchar(100),
  actions_expired      varchar(1) default 'N',
  session_expired      varchar(1) default 'N',
  role_action_list     mediumtext,
  action_list          mediumtext,
  primary key pk_sessions (sess_id),
  index idx_sessions_ssys_session_expired (ssys_ssys_id, session_expired),
  index idx_sessions_user_session_expired (user_user_id, session_expired)
);

call run_install_command('alter table users add column email varchar(255) not null', '42S21');