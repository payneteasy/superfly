call run_install_command('alter table subsystems add column private_key text', '42S21');
call run_install_command('alter table subsystems add column public_key text', '42S21');
call run_install_command('alter table subsystems add column encryption_algorithm varchar(16)', '42S21');

commit;
