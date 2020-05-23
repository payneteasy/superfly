call run_install_command('alter table users add column master_key varchar(64) after user_organization', '42S21');

commit;
