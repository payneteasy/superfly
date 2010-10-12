update users set name='admin' where user_name = 'admin';
update users set surname='adminsuperfly' where user_name = 'admin';
update users set secret_question='secret question' where user_name = 'admin';
update users set secret_answer='secret answer' where user_name = 'admin';


/*
insert into actions
      (
         action_name, action_description, ssys_ssys_id, log_action
      )
  values
    ('pci_admin', 'Allows to use Superfly admin console',
         (select ssys_id from subsystems where subsystem_name = 'superfly'), 'N')
  on duplicate key update action_description='Allows to use Superfly admin console',
                          log_action='N'
;


insert into users
      (
         comp_comp_id, user_name, user_password,salt, is_account_locked, logins_failed, last_login_date, email,name,surname,secret_question,secret_answer

      ) values 
    (null, 'pci_admin', '0d7d1771e08bc48f6fe90b14a89c505d344a0f6f1a54de3b10a93466cb235f96','3caffd7f8d4519cdd110ce3089431e7214635f4ff3f9235a94e3227e9b831e0f', 'N', 0, null,'pci@example.org','name','surname','','')

  on duplicate key update user_password='0d7d1771e08bc48f6fe90b14a89c505d344a0f6f1a54de3b10a93466cb235f96',
                          salt='3caffd7f8d4519cdd110ce3089431e7214635f4ff3f9235a94e3227e9b831e0f',
                          email='pci@example.org',name='name',surname='surname', 
                          secret_question='',secret_answer=''

;

delete from user_roles 
 where user_user_id in (select user_id from users where user_name = 'pci_admin')
       and 
       role_role_id in (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly'))
;
       

insert into user_roles
      (
         user_user_id, role_role_id
      ) values
    ((select user_id from users where user_name = 'pci_admin'), (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')))
;

delete from user_role_actions 
  where  user_user_id in (select user_id from users where user_name = 'pci_admin')
         and
         ract_ract_id in (select ract_id from role_actions where role_role_id = (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')) and actn_actn_id = (select actn_id from actions where action_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')))
;
 

insert into user_role_actions
      (
         user_user_id, ract_ract_id
      ) values
      ((select user_id from users where user_name = 'pci_admin'),
            (select ract_id from role_actions where role_role_id = (select role_id from roles where role_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly')) and actn_actn_id = (select actn_id from actions where action_name = 'admin' and ssys_ssys_id = (select ssys_id from subsystems where subsystem_name = 'superfly'))))
;
*/

update users set hotp_counter = 0 where hotp_counter is null;
update users set hotp_salt = sha1(concat(user_name, user_password, unix_timestamp()));