-- new users require OTP unless it is switched off explicitly; existing rows are left as they are
call run_install_command('alter table users alter column is_otp_optional set default "N"', '42S21');
