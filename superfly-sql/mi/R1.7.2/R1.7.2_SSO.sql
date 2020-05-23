call run_install_command('alter table users add column master_key varchar(64) after user_organization', '42S21');
call run_install_command('alter table users add column otp_otp_type_id int(10) after master_key', '42S21');



drop table if exists otp_types;

create table otp_types(
  otp_type_id          int(10) auto_increment,
  otp_name             varchar(64) not null,
  otp_code             varchar(16) not null,
  primary key pk_otp_types(otp_type_id)
) engine = innodb;


commit;
