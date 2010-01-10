insert into subsystems
      (
         subsystem_name, callback_information, fixed
      )
  values('superfly', null, 'Y');

insert into actions
      (
         action_name, action_description, ssys_ssys_id, log_action
      )
  values
    ('admin', 'Allows to use Superfly admin console',
         (select ssys_id from subsystems where subsystem_name = 'superfly'), 'N')
;

insert into roles
      (
         role_name, principal_name, ssys_ssys_id
      ) values
      ('admin', 'admin', (select ssys_id from subsystems where subsystem_name = 'superfly'))
;

insert into role_actions
      (
         role_role_id, actn_actn_id
      ) values
    ((select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')), (select actn_id from actions where action_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')))
;

insert into users
      (
         comp_comp_id, user_name, user_password, is_account_locked, logins_failed, last_login_date
      ) values 
    (null, 'admin', '123admin123', 'N', 0, null)
;

insert into user_roles
      (
         user_user_id, role_role_id
      ) values
    ((select user_id from users where user_name = 'admin'), (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')))
;

insert into user_role_actions
      (
         user_user_id, ract_ract_id
      ) values
      ((select user_id from users where user_name = 'admin'),
            (select ract_id from role_actions where role_role_id = (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')) and actn_actn_id = (select actn_id from actions where action_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly'))))
;
