-- register_user must not let one subsystem take over an uncompleted user that holds roles in another subsystem.
-- Run against a database prepared by create_test_database.sh: mysql ... ssotest < register_user_check.sql.
-- Any failed check aborts with SQLSTATE 45000.
delete from user_roles where user_user_id in (select user_id from users where user_name like 'regcheck-%');
delete from user_history where user_user_id in (select user_id from users where user_name like 'regcheck-%');
delete from users where user_name like 'regcheck-%';
delete from roles where role_name like 'regcheck-%';
delete from subsystems where subsystem_name like 'regcheck-%';

insert into subsystems (subsystem_name, landing_url, subsystem_url, subsystem_title)
values ('regcheck-a', 'http://a', 'http://a', 'a'), ('regcheck-b', 'http://b', 'http://b', 'b');
insert into roles (role_name, principal_name, ssys_ssys_id)
select 'regcheck-role', 'regcheck-p', ssys_id from subsystems where subsystem_name like 'regcheck-%';

drop procedure if exists regcheck_assert;
delimiter $$
create procedure regcheck_assert(i_ok tinyint, i_message varchar(100))
begin
  if i_ok is null or i_ok = 0 then
    signal sqlstate '45000' set message_text = i_message;
  end if;
end
$$
delimiter ;

-- subsystem B registers an uncompleted user holding a role in B
call register_user('regcheck-u1', 'hash-b', 'e@e', 'regcheck-b', 'regcheck-p', 'n', 's', 'q', 'a', 'salt', 'N', 'hs', null, 'org', null, @id_b);
call regcheck_assert((select count(*) = 1 from user_roles ur join roles r on r.role_id = ur.role_role_id
                       join subsystems s on s.ssys_id = r.ssys_ssys_id
                       where ur.user_user_id = @id_b and s.subsystem_name = 'regcheck-b'),
                     'setup: the user must hold a role in subsystem B');

-- subsystem A must not take the user over
call register_user('regcheck-u1', 'hash-a', 'e@e', 'regcheck-a', 'regcheck-p', 'n', 's', 'q', 'a', 'salt', 'N', 'hs', null, 'org', null, @id_a);
call regcheck_assert((select user_password = 'hash-b' and user_id = @id_b from users where user_name = 'regcheck-u1'),
                     'subsystem A must not recreate the user of subsystem B');
call regcheck_assert((select count(*) = 1 from user_roles ur join roles r on r.role_id = ur.role_role_id
                       join subsystems s on s.ssys_id = r.ssys_ssys_id
                       where ur.user_user_id = @id_b and s.subsystem_name = 'regcheck-b'),
                     'the role of subsystem B must survive');

-- the owning subsystem may register the uncompleted user again
call register_user('regcheck-u1', 'hash-b2', 'e@e', 'regcheck-b', 'regcheck-p', 'n', 's', 'q', 'a', 'salt', 'N', 'hs', null, 'org', null, @id_b2);
call regcheck_assert((select user_password = 'hash-b2' and user_id = @id_b2 from users where user_name = 'regcheck-u1'),
                     'subsystem B must be able to re-register its own uncompleted user');

-- an uncompleted user without roles is free to take
call register_user('regcheck-u2', 'hash-b', 'e@e', 'regcheck-b', null, 'n', 's', 'q', 'a', 'salt', 'N', 'hs', null, 'org', null, @id_n);
call register_user('regcheck-u2', 'hash-a', 'e@e', 'regcheck-a', 'regcheck-p', 'n', 's', 'q', 'a', 'salt', 'N', 'hs', null, 'org', null, @id_n2);
call regcheck_assert((select user_password = 'hash-a' from users where user_name = 'regcheck-u2'),
                     'an uncompleted user without roles must be re-registrable');

drop procedure regcheck_assert;
delete from user_roles where user_user_id in (select user_id from users where user_name like 'regcheck-%');
delete from user_history where user_user_id in (select user_id from users where user_name like 'regcheck-%');
delete from users where user_name like 'regcheck-%';
delete from roles where role_name like 'regcheck-%';
delete from subsystems where subsystem_name like 'regcheck-%';
select 'REGISTER USER CHECK OK' result;
