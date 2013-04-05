call run_install_command('alter table smtp_servers add column is_ssl char(1) not null default "N"', '42S21');
call run_install_command('alter table users add column completed varchar(1)', '42S21');

commit;