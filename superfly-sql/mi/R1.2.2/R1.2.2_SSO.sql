call run_install_command('alter table users add column public_key text;', '42S21');
alter table user_history modify salt varchar(64);