drop procedure if exists list_users;
delimiter $$
create procedure list_users(i_subsystem_name varchar(32), i_principal_list text
)
 main_sql:
  begin
    select r.role_name,
           r.principal_name,
           ss.callback_information,
           a.action_name role_action_name,
           a.log_action role_log_action
      from             user_roles ur
                     join
                       roles r
                     on r.role_id = ur.role_role_id
                        and instr(concat(',', i_principal_list, ','),
                                  concat(',', r.principal_name, ',')
                           )
                   join
                     subsystems ss
                   on r.ssys_ssys_id = ss.ssys_id and ss.allow_list_users = 'Y'
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
     where ss.subsystem_name = i_subsystem_name
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
                      and instr(concat(',', i_principal_list, ','),
                                concat(',', r.principal_name, ',')
                         )
                 join
                   subsystems ss
                 on r.ssys_ssys_id = ss.ssys_id and ss.allow_list_users = 'Y'
               join
                 user_role_actions ura
               on ura.user_user_id = ur.user_user_id
             join
               role_actions ra
             on ura.ract_ract_id = ra.ract_id and ra.role_role_id = r.role_id
           join
             actions a
           on ra.actn_actn_id = a.actn_id and a.ssys_ssys_id = ss.ssys_id
     where ss.subsystem_name = i_subsystem_name;
  end
;
$$
delimiter ;
call save_routine_information('list_users',
                              concat_ws(',',
                                        'role_name varchar',
                                        'principal_name varchar',
                                        'callback_information varchar',
                                        'role_action_name varchar',
                                        'role_log_action varchar'
                              )
     );