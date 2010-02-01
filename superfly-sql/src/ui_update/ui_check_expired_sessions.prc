drop procedure if exists ui_check_expired_sessions;
delimiter $$
create procedure ui_check_expired_sessions(i_ssys_id int(10),
                                           i_role_id int(10),
                                           i_user_id int(10),
                                           i_grop_id int(10)
)
 main_sql:
  begin
    declare v_ssys_id   int(10);

    if i_user_id is null then
      if i_ssys_id is not null then
        set v_ssys_id   = i_ssys_id;
      elseif i_role_id is not null then
        select ssys_ssys_id
          into v_ssys_id
          from roles
         where role_id = i_role_id;
      elseif i_grop_id is not null then
        select ssys_ssys_id
          into v_ssys_id
          from groups
         where grop_id = i_grop_id;
      end if;

      if v_ssys_id is null then
        leave main_sql;
      end if;

      update sessions ses,
             (select s.sess_id,
                     group_concat(concat(r.role_id, ':', ra.ract_id)
                                    order by r.role_id, ra.ract_id
                     )
                       ract_list
                from             sessions s
                               left join
                                 user_roles ur
                               on ur.user_user_id = s.user_user_id
                             left join
                               roles r
                             on r.role_id = ur.role_role_id
                           left join
                             subsystems ss
                           on r.ssys_ssys_id = ss.ssys_id
                              and ss.ssys_id = s.ssys_ssys_id
                         left join
                           user_role_actions ura
                         on ura.user_user_id = ur.user_user_id
                       left join
                         role_actions ra
                       on ura.ract_ract_id = ra.ract_id
                          and ra.role_role_id = r.role_id
                     left join
                       actions a
                     on ra.actn_actn_id = a.actn_id
                        and a.ssys_ssys_id = ss.ssys_id
               where s.session_expired = 'N' and s.ssys_ssys_id = v_ssys_id
              group by s.sess_id) ract,
             (select s.sess_id,
                     group_concat(concat(r.role_id, ':', a.actn_id)
                                    order by r.role_id, a.actn_id
                     )
                       act_list
                from               sessions s
                                 left join
                                   user_roles ur
                                 on ur.user_user_id = s.user_user_id
                               left join
                                 roles r
                               on r.role_id = ur.role_role_id
                             left join
                               subsystems ss
                             on r.ssys_ssys_id = ss.ssys_id
                                and ss.ssys_id = s.ssys_ssys_id
                           left join
                             role_groups rg
                           on rg.role_role_id = r.role_id
                         left join
                           groups g
                         on rg.grop_grop_id = g.grop_id
                       left join
                         group_actions ga
                       on ga.grop_grop_id = g.grop_id
                          and g.ssys_ssys_id = ss.ssys_id
                     left join
                       actions a
                     on a.ssys_ssys_id = ss.ssys_id
                        and ga.actn_actn_id = a.actn_id
               where s.session_expired = 'N' and s.ssys_ssys_id = v_ssys_id
              group by s.sess_id) act
         set ses.actions_expired    = 'Y'
       where     ses.sess_id = ract.sess_id
             and ses.sess_id = act.sess_id
             and ses.ssys_ssys_id = v_ssys_id
             and(coalesce(ses.role_action_list, '-1') !=
                   coalesce(ract.ract_list, '-1')
                 or coalesce(ses.action_list, '-1') !=
                     coalesce(act.act_list, '-1'));
    else
      update sessions ses,
             (select s.sess_id,
                     group_concat(concat(r.role_id, ':', ra.ract_id)
                                    order by r.role_id, ra.ract_id
                     )
                       ract_list
                from             sessions s
                               left join
                                 user_roles ur
                               on ur.user_user_id = s.user_user_id
                             left join
                               roles r
                             on r.role_id = ur.role_role_id
                           left join
                             subsystems ss
                           on r.ssys_ssys_id = ss.ssys_id
                              and ss.ssys_id = s.ssys_ssys_id
                         left join
                           user_role_actions ura
                         on ura.user_user_id = ur.user_user_id
                       left join
                         role_actions ra
                       on ura.ract_ract_id = ra.ract_id
                          and ra.role_role_id = r.role_id
                     left join
                       actions a
                     on ra.actn_actn_id = a.actn_id
                        and a.ssys_ssys_id = ss.ssys_id
               where s.session_expired = 'N' and s.user_user_id = i_user_id
              group by s.sess_id) ract,
             (select s.sess_id,
                     group_concat(concat(r.role_id, ':', a.actn_id)
                                    order by r.role_id, a.actn_id
                     )
                       act_list
                from               sessions s
                                 left join
                                   user_roles ur
                                 on ur.user_user_id = s.user_user_id
                               left join
                                 roles r
                               on r.role_id = ur.role_role_id
                             left join
                               subsystems ss
                             on r.ssys_ssys_id = ss.ssys_id
                                and ss.ssys_id = s.ssys_ssys_id
                           left join
                             role_groups rg
                           on rg.role_role_id = r.role_id
                         left join
                           groups g
                         on rg.grop_grop_id = g.grop_id
                       left join
                         group_actions ga
                       on ga.grop_grop_id = g.grop_id
                          and g.ssys_ssys_id = ss.ssys_id
                     left join
                       actions a
                     on a.ssys_ssys_id = ss.ssys_id
                        and ga.actn_actn_id = a.actn_id
               where s.session_expired = 'N' and s.user_user_id = i_user_id
              group by s.sess_id) act
         set ses.actions_expired    = 'Y'
       where     ses.sess_id = ract.sess_id
             and ses.sess_id = act.sess_id
             and ses.ssys_ssys_id = v_ssys_id
             and(coalesce(ses.role_action_list, '-1') !=
                   coalesce(ract.ract_list, '-1')
                 or coalesce(ses.action_list, '-1') !=
                     coalesce(act.act_list, '-1'));
    end if;
  end
$$
delimiter ;