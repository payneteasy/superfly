-- Lockout/expiry regression check for the stored procedures. Run against a database prepared by
-- create_test_database.sh: mysql ... ssotest < lockout_check.sql. Any failed check aborts with SQLSTATE 45000.
delete from users where user_name like 'lockcheck-%';

insert into users (user_name, user_password, is_account_locked, logins_failed, hotp_logins_failed, email, name, surname,
                   secret_question, secret_answer, hotp_salt, create_date, is_otp_optional)
values ('lockcheck-otp', 'h', 'N', null, null, 'e', 'n', 's', 'q', 'a', 'hs', now(), 'N'),
       ('lockcheck-expire', 'old-hash', 'Y', 5, 5, 'e', 'n', 's', 'q', 'a', 'hs', now(), 'N'),
       ('lockcheck-manual', 'old-hash', 'Y', 5, 5, 'e', 'n', 's', 'q', 'a', 'hs', now(), 'N');

drop procedure if exists lockcheck_assert;
delimiter $$
create procedure lockcheck_assert(i_ok tinyint, i_message varchar(100))
begin
  if i_ok is null or i_ok = 0 then
    signal sqlstate '45000' set message_text = i_message;
  end if;
end
$$
delimiter ;

-- OTP counter starting from NULL must count and lock after the maximum
call increment_hotp_logins_failed('lockcheck-otp');
call lockcheck_assert((select hotp_logins_failed = 1 from users where user_name = 'lockcheck-otp'),
                      'increment from NULL must give 1');
call increment_hotp_logins_failed('lockcheck-otp');
call increment_hotp_logins_failed('lockcheck-otp');
call login_locked('lockcheck-otp', 3, 'HOTP');
call lockcheck_assert((select is_account_locked = 'Y' from users where user_name = 'lockcheck-otp'),
                      'account must be locked after max OTP failures');

-- manual unlock must zero the OTP counter, otherwise the next failure re-locks at once
call ui_unlock_user((select user_id from users where user_name = 'lockcheck-otp'));
call lockcheck_assert((select is_account_locked = 'N' and hotp_logins_failed = 0 and logins_failed is null
                         from users where user_name = 'lockcheck-otp'),
                      'ui_unlock_user must zero hotp_logins_failed');
call increment_hotp_logins_failed('lockcheck-otp');
call login_locked('lockcheck-otp', 3, 'HOTP');
call lockcheck_assert((select is_account_locked = 'N' from users where user_name = 'lockcheck-otp'),
                      'one OTP failure after unlock must not lock');
update users set is_account_locked = 'Y', is_account_suspended = 'Y', hotp_logins_failed = 3 where user_name = 'lockcheck-otp';
call ui_unlock_suspended_user((select user_id from users where user_name = 'lockcheck-otp'), 'tmp-hash');
call lockcheck_assert((select is_account_locked = 'N' and hotp_logins_failed = 0
                         from users where user_name = 'lockcheck-otp'),
                      'ui_unlock_suspended_user must zero hotp_logins_failed');

-- explicit reset with a password unlocks and zeroes (not NULLs) the OTP counter
call reset_password((select user_id from users where user_name = 'lockcheck-manual'), 'new-hash');
call lockcheck_assert((select is_account_locked = 'N' and hotp_logins_failed = 0 and logins_failed is null
                                and user_password = 'new-hash' and is_password_temp = 'Y'
                         from users where user_name = 'lockcheck-manual'),
                      'reset with password must unlock, set hotp_logins_failed=0, store the password');
call increment_hotp_logins_failed('lockcheck-manual');
call lockcheck_assert((select hotp_logins_failed = 1 from users where user_name = 'lockcheck-manual'),
                      'increment after reset must give 1');

-- expiry (NULL password) must not unlock a locked account nor clear the counters
call reset_password((select user_id from users where user_name = 'lockcheck-expire'), null);
call lockcheck_assert((select is_account_locked = 'Y' and logins_failed = 5 and hotp_logins_failed = 5
                                and user_password = 'old-hash' and is_password_temp = 'Y'
                         from users where user_name = 'lockcheck-expire'),
                      'expiry must keep lock and counters and only mark the password temporary');

drop procedure lockcheck_assert;
delete from users where user_name like 'lockcheck-%';
select 'LOCKOUT CHECK OK' result;
