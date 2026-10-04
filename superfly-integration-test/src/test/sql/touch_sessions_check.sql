-- touch_sessions must extend only the listed sessions of the caller's subsystem. Run against a database prepared by
-- create_test_database.sh: mysql ... ssotest < touch_sessions_check.sql. Any failed check aborts with SQLSTATE 45000.
delete from sessions where callback_information = 'touchcheck';
delete from sso_sessions where identifier like 'touchcheck-%';
delete from users where user_name = 'touchcheck-user';
delete from subsystems where subsystem_name like 'touchcheck-%';

insert into users (user_name, user_password, is_account_locked, email, name, surname, secret_question, secret_answer,
                   hotp_salt, create_date)
values ('touchcheck-user', 'h', 'N', 'e', 'n', 's', 'q', 'a', 'hs', now());
set @uid = last_insert_id();
insert into subsystems (subsystem_name, landing_url, subsystem_url, subsystem_title)
values ('touchcheck-a', 'http://a', 'http://a', 'a'), ('touchcheck-b', 'http://b', 'http://b', 'b');
set @sys_a = (select ssys_id from subsystems where subsystem_name = 'touchcheck-a');
set @sys_b = (select ssys_id from subsystems where subsystem_name = 'touchcheck-b');
insert into sso_sessions (identifier, user_user_id, created_date, access_date)
values ('touchcheck-1', @uid, '2000-01-01', '2000-01-01'),
       ('touchcheck-2', @uid, '2000-01-01', '2000-01-01'),
       ('touchcheck-3', @uid, '2000-01-01', '2000-01-01');
insert into sessions (start_date, user_user_id, ssys_ssys_id, callback_information, ssos_ssos_id)
select now(), @uid, if(identifier = 'touchcheck-3', @sys_b, @sys_a), 'touchcheck', ssos_id
  from sso_sessions where identifier like 'touchcheck-%';
set @s1 = (select s.sess_id from sessions s join sso_sessions ss on ss.ssos_id = s.ssos_ssos_id where ss.identifier = 'touchcheck-1');
set @s2 = (select s.sess_id from sessions s join sso_sessions ss on ss.ssos_id = s.ssos_ssos_id where ss.identifier = 'touchcheck-2');
set @s3 = (select s.sess_id from sessions s join sso_sessions ss on ss.ssos_id = s.ssos_ssos_id where ss.identifier = 'touchcheck-3');

drop procedure if exists touchcheck_assert;
drop procedure if exists touchcheck_reset;
delimiter $$
create procedure touchcheck_assert(i_expected varchar(10), i_message varchar(100))
begin
  -- i_expected lists the identifiers' suffixes that must have been touched
  if (select ifnull(group_concat(substr(identifier, 12) order by identifier), '')
        from sso_sessions where identifier like 'touchcheck-%' and access_date > '2001-01-01') <> i_expected then
    signal sqlstate '45000' set message_text = i_message;
  end if;
end
$$
create procedure touchcheck_reset()
begin
  update sso_sessions set access_date = '2000-01-01' where identifier like 'touchcheck-%';
end
$$
delimiter ;

call touch_sessions(cast(@s1 as char), 'touchcheck-a');
call touchcheck_assert('1', 'only the listed session of the caller must be touched');

call touchcheck_reset();
call touch_sessions(cast(@s3 as char), 'touchcheck-a');
call touchcheck_assert('', 'a session of another subsystem must not be touched');

call touchcheck_reset();
call touch_sessions(concat(@s1, ',', @s2, ',', @s3), 'touchcheck-a');
call touchcheck_assert('1,2', 'foreign ids in the list must be skipped');

call touchcheck_reset();
call touch_sessions(concat(@s1, ',x'), 'touchcheck-a');
call touchcheck_assert('', 'a malformed list must touch nothing');
call touch_sessions(concat(@s1, ' OR 1=1'), 'touchcheck-a');
call touchcheck_assert('', 'a list with garbage must touch nothing');
call touch_sessions('', 'touchcheck-a');
call touchcheck_assert('', 'an empty list must touch nothing');

call touch_sessions(cast(@s1 as char), null);
call touchcheck_assert('', 'a caller without subsystem must touch nothing');
call touch_sessions(cast(@s1 as char), 'no-such-subsystem');
call touchcheck_assert('', 'an unknown subsystem must touch nothing');

drop procedure touchcheck_assert;
drop procedure touchcheck_reset;
delete from sessions where callback_information = 'touchcheck';
delete from sso_sessions where identifier like 'touchcheck-%';
delete from users where user_name = 'touchcheck-user';
delete from subsystems where subsystem_name like 'touchcheck-%';
select 'TOUCH SESSIONS CHECK OK' result;
