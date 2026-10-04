-- OTP defaults and TOTP anti-replay regression check for the stored procedures. Run against a database prepared by
-- create_test_database.sh: mysql ... ssotest < otp_check.sql. Any failed check aborts with SQLSTATE 45000.
delete from user_history where user_user_id in (select user_id from users where user_name like 'otpcheck-%');
delete from users where user_name like 'otpcheck-%';

drop procedure if exists otpcheck_assert;
delimiter $$
create procedure otpcheck_assert(i_ok tinyint, i_message varchar(100))
begin
  if i_ok is null or i_ok = 0 then
    signal sqlstate '45000' set message_text = i_message;
  end if;
end
$$
delimiter ;

-- a user registered without an explicit flag must require OTP
call register_user('otpcheck-new', 'h', 'e@e', null, null, 'n', 's', 'q', 'a', 'salt', 'N', 'hs', null, 'org', null, @new_user_id);
call otpcheck_assert((select is_otp_optional = 'N' and otp_last_used_step is null
                        from users where user_name = 'otpcheck-new'),
                     'a new user must require OTP and have no used TOTP step');

-- a step is accepted once and only if it is above the last used one
call save_otp_last_used_step('otpcheck-new', 100);
call otpcheck_assert((select otp_last_used_step = 100 from users where user_name = 'otpcheck-new'),
                     'the first step must be stored');
call save_otp_last_used_step('otpcheck-new', 100);
call otpcheck_assert((select otp_last_used_step = 100 from users where user_name = 'otpcheck-new'),
                     'a replayed step must not change the stored one');
call save_otp_last_used_step('otpcheck-new', 99);
call otpcheck_assert((select otp_last_used_step = 100 from users where user_name = 'otpcheck-new'),
                     'an older step must not lower the stored one');
call save_otp_last_used_step('otpcheck-new', 101);
call otpcheck_assert((select otp_last_used_step = 101 from users where user_name = 'otpcheck-new'),
                     'a newer step must be stored');

drop procedure otpcheck_assert;
delete from user_history where user_user_id = @new_user_id;
delete from users where user_name like 'otpcheck-%';
select 'OTP CHECK OK' result;
