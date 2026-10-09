-- OTP master key: wider column for the v2 ciphertext
-- v2 ciphertext of the OTP master key (version prefix + nonce + AES-GCM tag) is 83 characters
call run_install_command('alter table users modify column master_key varchar(128)', '42S21');

-- OTP lockout counter: NULL values become 0
-- NULL + 1 is NULL: a NULL hotp_logins_failed counter never reached the lockout threshold
update users set hotp_logins_failed = 0 where hotp_logins_failed is null;
commit;

-- OTP defaults and the last used TOTP step
-- new users require OTP unless it is switched off explicitly; existing rows are left as they are
call run_install_command('alter table users alter column is_otp_optional set default "N"', '42S21');

-- time step of the last accepted TOTP code: a code is accepted only for a step above this one
call run_install_command('alter table users add column otp_last_used_step bigint null', '42S21');

-- pending OTP master key
-- a key issued by resetGoogleAuthMasterKey waits here until a code from it is confirmed; master_key keeps working
call run_install_command('alter table users add column otp_pending_master_key varchar(128) null', '42S21');

-- default admin password and HOTP salt
-- PCI DSS 2.1: an admin that still has the default password from the documentation must change it at the first login.
-- A password that is already changed (including the pbkdf2 rehash made at login) does not match and stays as is.
update users set is_password_temp = 'Y'
 where user_name = 'admin'
   and user_password = '0d7d1771e08bc48f6fe90b14a89c505d344a0f6f1a54de3b10a93466cb235f96'
   and salt = '3caffd7f8d4519cdd110ce3089431e7214635f4ff3f9235a94e3227e9b831e0f';

-- the HOTP salt of the default admin was published together with the password
update users set hotp_salt = sha1(uuid())
 where user_name = 'admin'
   and hotp_salt = 'f81ead99b99b7f0a91a441621ab1d1248860848f';

-- subsystem tokens, legacy password history and SMTP passwords
-- subsystem main tokens are stored as 'sha256:' + hex(SHA-256(token)); 7 + 64 characters do not fit varchar(64)
call run_install_command('alter table subsystems modify column subsystem_token varchar(80)', '42S21');

-- clients keep working with their current tokens; an already converted value is not hashed again
update subsystems
   set subsystem_token = concat('sha256:', sha2(subsystem_token, 256))
 where subsystem_token is not null
   and subsystem_token <> ''
   and subsystem_token not like 'sha256:%';

-- legacy (not pbkdf2) hashes in user_history outside the reuse check window are dropped: the window is the 5 newest
-- rows of a user (the one standing for the current password and the 4 before it); the rows inside it are replaced
-- by pbkdf2 hashes at login or leave the window with the next password changes
delete uh
  from user_history uh
       inner join (select a.user_user_id, a.number_history
                     from user_history a
                          inner join user_history b
                                  on b.user_user_id = a.user_user_id and b.number_history > a.number_history
                    group by a.user_user_id, a.number_history
                   having count(*) >= 5) old
               on old.user_user_id = uh.user_user_id and old.number_history = uh.number_history
 where uh.user_password not like 'pbkdf2-sha256$%';

-- SMTP server passwords are stored as CryptoService ciphertext ("v2:" + base64), which does not fit varchar(64)
call run_install_command('alter table smtp_servers modify column password varchar(255)', '42S21');
