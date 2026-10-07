-- a key issued by resetGoogleAuthMasterKey waits here until a code from it is confirmed; master_key keeps working
call run_install_command('alter table users add column otp_pending_master_key varchar(128) null', '42S21');
