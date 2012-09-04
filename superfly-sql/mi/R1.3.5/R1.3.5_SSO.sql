call run_install_command('alter table unauthorised_access add index idx_unauthorised_access_printed_user_name (printed_user_name)', '42S21');

commit;