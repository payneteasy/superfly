drop procedure if exists ui_get_user_role_actions;
delimiter $$
create procedure ui_get_user_role_actions(i_user_id int(10),
                                          i_ssys_list text,
                                          i_action_name varchar(100),
                                          i_role_name varchar(100)
)
 main_sql:
  begin
    select u.user_id,
           u.user_name,
           ss.subsystem_name role_subsystem_name,
           r.role_id,
           r.role_name,
           null role_action_ract_id,
           a.actn_id role_action_id,
           a.action_name role_action_name
      from               users u
                       join
                         user_roles ur
                       on ur.user_user_id = u.user_id
                     join
                       roles r
                     on r.role_id = ur.role_role_id
                        and r.role_name like
                             concat('%', coalesce(i_role_name, r.role_name), '%'
                             )
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
              and a.action_name like
                   concat('%', coalesce(i_action_name, a.action_name), '%')
     where ur.user_user_id = i_user_id
           and instr(concat(',', coalesce(i_ssys_list, ss.ssys_id), ','),
                     concat(',', ss.ssys_id, ',')
              ) > 0
    union
    select u.user_id,
           u.user_name,
           ss.subsystem_name role_subsystem_name,
           r.role_id,
           r.role_name,
           ra.ract_id role_action_ract_id,
           a.actn_id role_action_id,
           a.action_name role_action_name
      from             users u
                     join
                       user_roles ur
                     on ur.user_user_id = u.user_id
                   join
                     roles r
                   on r.role_id = ur.role_role_id
                      and r.role_name like
                           concat('%', coalesce(i_role_name, r.role_name), '%')
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
              and a.action_name like
                   concat('%', coalesce(i_action_name, a.action_name), '%')
     where ur.user_user_id = i_user_id
           and instr(concat(',', coalesce(i_ssys_list, ss.ssys_id), ','),
                     concat(',', ss.ssys_id, ',')
              ) > 0;
  end
$$
delimiter ;
call save_routine_information('ui_get_user_role_actions',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar',
                                        'role_subsystem_name varchar',
                                        'role_id int',
                                        'role_name varchar',
                                        'role_action_ract_id int',
                                        'role_action_id int',
                                        'role_action_name varchar'
                              )
     );