drop procedure if exists get_user_actions;
delimiter $$
create procedure get_user_actions(i_user_name varchar(32),
                                  i_user_password varchar(32),
                                  i_subsystem_name varchar(32),
                                  i_ip_address varchar(15),
                                  i_session_info text
)
 main_sql:
  begin
    declare v_user_id   int(10);

    set v_user_id   = null;

    select user_id
      into v_user_id
      from users u
     where     u.user_name = i_user_name
           and u.user_password = i_user_password
           and coalesce(u.is_account_locked, 'N') = 'N';

    if v_user_id is null then
      update users u
         set u.logins_failed    = coalesce(u.logins_failed, 0) + 1
       where u.user_name = i_user_name
             and coalesce(u.is_account_locked, 'N') = 'N';

      insert into unauthorised_access
            (
               printed_user_name,
               printed_password,
               access_date,
               ip_address,
               session_info
            )
      values (i_user_name, i_user_password, now(), ip_address, session_info);

      commit;

      select null
        from dual
       where false;

      leave main_sql;
    end if;

    update users u
       set u.last_login_date    = now()
     where u.user_name = i_user_name
           and coalesce(u.is_account_locked, 'N') = 'N';

    select r.role_name,
           r.principal_name,
           ss.callback_information,
           a.action_name role_action_name,
           a.log_action role_log_action
      from             user_roles ur
                     join
                       roles r
                     on r.role_id = ur.role_role_id
                   join
                     subsystems ss
                   on r.ssys_ssys_id = ss.ssys_id
                 join
                   role_groups rg
                 on rg.role_role_id = r.role_id
               join
                 groups g
               on rg.grop_grop_id = g.grop_id
             join
               group_actions ga
             on ga.grop_grop_id = g.grop_id and g.ssys_ssys_id = ss.ssys_id
           join
             actions a
           on a.ssys_ssys_id = ss.ssys_id and ga.actn_actn_id = a.actn_id
     where ur.user_user_id = v_user_id and ss.subsystem_name = i_subsystem_name
    union
    select r.role_name,
           r.principal_name,
           ss.callback_information,
           a.action_name role_action_name,
           a.log_action role_log_action
      from           user_roles ur
                   join
                     roles r
                   on r.role_id = ur.role_role_id
                 join
                   subsystems ss
                 on r.ssys_ssys_id = ss.ssys_id
               join
                 user_role_actions ura
               on ura.user_user_id = ur.user_user_id
             join
               role_actions ra
             on ura.ract_ract_id = ra.ract_id and ra.role_role_id = r.role_id
           join
             actions a
           on ra.actn_actn_id = a.actn_id and a.ssys_ssys_id = ss.ssys_id
     where ur.user_user_id = v_user_id and ss.subsystem_name = i_subsystem_name

;
  end
;
$$
delimiter ;
call save_routine_information('get_user_actions',
                              concat_ws(',',
                                        'role_name varchar',
                                        'principal_name varchar',
                                        'callback_information varchar',
                                        'role_action_name varchar',
                                        'role_log_action varchar'
                              )
     );