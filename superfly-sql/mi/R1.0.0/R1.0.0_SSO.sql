drop table if exists actions_log;
drop table if exists unauthorised_access;
drop table if exists user_preferences;
drop table if exists user_role_actions;
drop table if exists user_roles;
drop table if exists user_complects;
drop table if exists users;
drop table if exists complect_preferences;
drop table if exists complects;
drop table if exists preferences;
drop table if exists role_groups;
drop table if exists role_actions;
drop table if exists roles;
drop table if exists group_actions;
drop table if exists groups;
drop table if exists actions;
drop table if exists subsystems;
create table subsystems(
  ssys_id              int(10) auto_increment,
  subsystem_name       varchar(32),
  callback_information varchar(64),
  fixed                varchar(1),
	unique unq_subsystems_subsystem_name (subsystem_name),
  primary key pk_subsystems(ssys_id)
);
create table actions(
  actn_id            int(10) auto_increment,
  action_name        varchar(128),
  action_description varchar(512),
  ssys_ssys_id       int(10),
  log_action         varchar(1),
  primary key pk_actions (actn_id),
  unique unq_actions_action_name_subsystem (action_name, ssys_ssys_id),
  constraint fk_actions_subsystems foreign key (ssys_ssys_id) references subsystems (ssys_id)
);
create table groups(
  grop_id      int(10) auto_increment,
  group_name   varchar(32),
  ssys_ssys_id int(10),
  primary key pk_group(grop_id),
  unique unq_groups_grop_name (group_name, ssys_ssys_id),
  constraint fk_groups_subsystems foreign key (ssys_ssys_id) references subsystems (ssys_id)
);
create table group_actions(
  gpac_id      int(10) auto_increment,
  grop_grop_id int(10),
  actn_actn_id int(10),
  primary key pk_group_actions(gpac_id),
  constraint fk_group_actions_groups foreign key (grop_grop_id) references groups (grop_id),
  constraint fk_group_actions_acitions foreign key (actn_actn_id) references actions (actn_id)
);
create table roles(
  role_id        int(10) auto_increment,
  role_name      varchar(32),
	principal_name varchar(32),
  ssys_ssys_id int(10),
  primary key pk_roles(role_id),
	unique unq_roles_role_name (role_name, ssys_ssys_id),
  constraint fk_roles_subsystems foreign key (ssys_ssys_id) references subsystems (ssys_id)
);
create table role_groups(
  rlgp_id      int(10) auto_increment,
  role_role_id int(10),
  grop_grop_id int(10),
  primary key pk_role_groups(rlgp_id),
  constraint fk_role_groups_roles foreign key (role_role_id) references roles (role_id),
  constraint fk_role_groups_groups foreign key (grop_grop_id) references groups (grop_id)
);
create table role_actions(
  ract_id      int(10) auto_increment,
  role_role_id int(10),
  actn_actn_id int(10),
  primary key pk_role_actions (ract_id),
  constraint fk_role_actions_roles foreign key (role_role_id) references roles (role_id),
  constraint fk_role_actions_actions foreign key (actn_actn_id) references actions (actn_id)
);
create table preferences(
  pref_id                int(10) auto_increment,
  preference_name        varchar(128),
  default_value          text,
  preference_description varchar(512),
  ssys_ssys_id           int(10),
  primary key pk_preferences (pref_id),
  unique unq_preferences_preference_name (preference_name),
  constraint fk_preferences_subsystems foreign key (ssys_ssys_id) references subsystems (ssys_id)
);
create table complects(
  comp_id            int(10) auto_increment,
  complect_name      varchar(32),
  comp_comp_id       int(10),
  hierarchical_level int(10),
  ssys_ssys_id       int(10),
  full_path          varchar(100),
  primary key pk_complects(comp_id),
  unique unq_complects_complect_name (complect_name),
  constraint fk_complects_complects foreign key (comp_comp_id) references complects (comp_id),
  constraint fk_complects_subsystems foreign key (ssys_ssys_id) references subsystems (ssys_id)
);
create table complect_preferences(
  cppr_id      int(10) auto_increment,
  comp_comp_id int(10),
  pref_pref_id int(10),
  primary key pk_complect_preferences(cppr_id),
  constraint fk_complect_preferences_complects foreign key (comp_comp_id) references complects (comp_id),
  constraint fk_complect_preferences_preferences foreign key (pref_pref_id) references preferences (pref_id)
);
create table users(
  user_id           int(10) auto_increment,
  user_name         varchar(32),
  user_password     varchar(32),
  is_account_locked varchar(1),
  logins_failed     int(10),
  last_login_date   datetime,
  comp_comp_id      int(10),
  primary key pk_users (user_id),
  unique unq_users_user_name (user_name)
);
create table user_roles(
  urol_id      int(10) auto_increment,
  user_user_id int(10),
  role_role_id int(10),
  primary key pk_user_roles (urol_id),
  constraint fk_user_roles_users foreign key (user_user_id) references users (user_id),
  constraint fk_user_roles_roles foreign key (role_role_id) references roles (role_id)
);
create table user_role_actions(
  urac_id      int(10) auto_increment,
  user_user_id int(10),
  ract_ract_id int(10),
  primary key pk_user_role_actions (urac_id),
  constraint fk_user_role_actions_users foreign key (user_user_id) references users (user_id),
  constraint fk_user_role_actions_role_actions foreign key (ract_ract_id) references role_actions (ract_id)
);
create table user_complects(
  ucom_id      int(10) auto_increment,
  user_user_id int(10),
  comp_comp_id int(10),
  primary key pk_user_complects (ucom_id),
  constraint fk_user_complects_users foreign key (user_user_id) references users (user_id),
  constraint fk_user_complects_complects foreign key (comp_comp_id) references complects (comp_comp_id)
);
create table user_preferences(
  uprf_id      int(10) auto_increment,
  user_user_id int(10),
  pref_pref_id int(10),
  user_value   text,
  primary key pk_user_preferences (uprf_id),
  constraint fk_user_preferences_users foreign key (user_user_id) references users (user_id),
  constraint fk_user_preferences_preferences foreign key (pref_pref_id) references preferences (pref_id)
);
create table actions_log(
  alog_id       int(10) auto_increment,
  partition_key int(6),
  actn_actn_id  int(10),
	action_name   varchar(128),
  user_user_id  int(10),
	user_name     varchar(32),
  log_date      datetime,
  ip_address    varchar(15),
  session_info  text,
  primary key pk_operations_log (alog_id, partition_key),
  index idx_operations_log_action_name (action_name),
  index idx_operations_log_user_name (user_name),
  index idx_operations_log_log_date (log_date)
  )
  engine = innodb
  partition by range (partition_key)(
    partition operations_log_100000 values less than(100001)
  );
create table unauthorised_access(
  unac_id           int(10) auto_increment,
  printed_user_name varchar(32),
  printed_password  varchar(32),
  access_date       datetime,
  ip_address        varchar(15),
  session_info      text,
  primary key pk_unauthorised_access (unac_id),
  index idx_unauthorised_access_access_date (access_date)
);
