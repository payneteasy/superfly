-- v2 ciphertext of the OTP master key (version prefix + nonce + AES-GCM tag) is 83 characters
call run_install_command('alter table users modify column master_key varchar(128)', '42S21');
