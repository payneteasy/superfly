call run_install_command('alter table subsystems add column subsystem_token varchar(64) after subsystem_title', '42S21');

commit;
