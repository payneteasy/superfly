drop table if exists mysql_routines_return_arguments;

create table mysql_routines_return_arguments (
  routine_name varchar(128) not null,
  argument_name varchar(128) not null,
  argument_type varchar(128) not null,
	ordinal_number int(10),
  unique key unq_mysql_routines_arguments_name_type (routine_name, argument_name)
) engine=innodb;

\. save_routine_information.sql

\. run_install_command.sql

\. uninject.sql

\. get/get_user_actions.sql
