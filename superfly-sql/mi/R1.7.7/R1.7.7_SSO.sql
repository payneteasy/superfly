-- new users require OTP unless it is switched off explicitly; existing rows are left as they are
call run_install_command('alter table users alter column is_otp_optional set default "N"', '42S21');

-- time step of the last accepted TOTP code: a code is accepted only for a step above this one
call run_install_command('alter table users add column otp_last_used_step bigint null', '42S21');
