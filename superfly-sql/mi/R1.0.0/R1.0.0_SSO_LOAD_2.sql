insert into subsystems
      (
         subsystem_name, callback_information, fixed
      )
  values('test1', 'Call back link for test1', 'N');

insert into actions
      (
         action_name, action_description, ssys_ssys_id, log_action
      )
  values
    ('adminpage1', 'Allows to use admin page1',
         (select ssys_id from subsystems where subsystem_name = 'test1'), 'N'),
    ('adminpage2', 'Allows to use admin page2',
         (select ssys_id from subsystems where subsystem_name = 'test1'), 'N'),
    ('userpage1', 'Allows to use user page1',
         (select ssys_id from subsystems where subsystem_name = 'test1'), 'N'),
    ('userpage2', 'Allows to use user page2',
         (select ssys_id from subsystems where subsystem_name = 'test1'), 'N')
;

insert into roles
      (
         role_name, principal_name, ssys_ssys_id
      ) values
      ('admin', 'admin', (select ssys_id from subsystems where subsystem_name = 'test1')),
      ('user', 'user', (select ssys_id from subsystems where subsystem_name = 'test1'))
;

insert into role_actions
      (
         role_role_id, actn_actn_id
      ) values
    ((select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')), (select actn_id from actions where action_name = 'adminpage1' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1'))),
    ((select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')), (select actn_id from actions where action_name = 'adminpage2' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1'))),
    ((select role_id from roles where role_name = 'user' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')), (select actn_id from actions where action_name = 'userpage1' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1'))),
    ((select role_id from roles where role_name = 'user' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')), (select actn_id from actions where action_name = 'userpage2' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')))
;

insert into users
      (
         comp_comp_id, user_name, user_password, is_account_locked, logins_failed, last_login_date
      ) values 
    (null, 'user', 'password', 'N', 0, null)
;

insert into user_roles
      (
         user_user_id, role_role_id
      ) values
    ((select user_id from users where user_name = 'admin'), (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1'))),
    ((select user_id from users where user_name = 'user'), (select role_id from roles where role_name = 'user' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')))
;

insert into user_role_actions
      (
         user_user_id, ract_ract_id
      ) values
      ((select user_id from users where user_name = 'admin'),
            (select ract_id from role_actions where role_role_id = (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')) and actn_actn_id = (select actn_id from actions where action_name = 'adminpage1' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')))),
      ((select user_id from users where user_name = 'admin'),
            (select ract_id from role_actions where role_role_id = (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')) and actn_actn_id = (select actn_id from actions where action_name = 'adminpage2' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')))),
      ((select user_id from users where user_name = 'user'),
            (select ract_id from role_actions where role_role_id = (select role_id from roles where role_name = 'user' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')) and actn_actn_id = (select actn_id from actions where action_name = 'userpage1' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')))),
      ((select user_id from users where user_name = 'user'),
            (select ract_id from role_actions where role_role_id = (select role_id from roles where role_name = 'user' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1')) and actn_actn_id = (select actn_id from actions where action_name = 'userpage2' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'test1'))))
;
