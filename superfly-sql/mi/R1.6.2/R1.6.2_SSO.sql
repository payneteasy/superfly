call run_install_command('alter table subsystems add column send_callbacks varchar(1) not null default "Y"', '42S21');

commit;