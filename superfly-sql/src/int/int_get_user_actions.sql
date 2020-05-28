drop procedure if exists int_get_user_actions;
delimiter $$
create procedure int_get_user_actions(i_user_id int(10),
                                      i_subsystem_name varchar(32),
                                      i_ip_address varchar(15),
                                      i_session_info text,
                                      i_ignore_temp varchar(1),
                                      i_sso_session_id int(10)
)
 main_sql:
  begin
    declare v_temp      varchar(1);
    declare v_sess_id	int(10);
    declare v_user_name varchar(32);
    declare v_otp_code  varchar(32);
    declare v_otp_type_id int(10);

    set session group_concat_max_len   = 64 * 1028;

    if i_user_id is null then
      commit; -- to save unauthorized_access INSERT

      select null
        from dual
       where false;

      leave main_sql;
    end if;

    select user_name, is_password_temp
           , otp.otp_type_id
           , otp.otp_code
      into v_user_name, v_temp ,v_otp_type_id ,v_otp_code
      from users u
         left join otp_types otp on u.otp_otp_type_id=otp.otp_type_id
     where     u.user_id = i_user_id;


    if i_ignore_temp = 'Y' or v_temp <> 'Y' then

        insert into sessions
              (
                 start_date, user_user_id, ssys_ssys_id, callback_information, role_action_list, action_list, ssos_ssos_id
              )
          select now(),
                 i_user_id,
                 ssys_id,
                 callback_information,
                 (select group_concat(concat(r.role_id, ':', ra.ract_id)
                                        order by r.role_id, ra.ract_id
                         )
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
                           on ura.ract_ract_id = ra.ract_id
                              and ra.role_role_id = r.role_id
                         join
                           actions a
                         on ra.actn_actn_id = a.actn_id
                            and a.ssys_ssys_id = ss.ssys_id
                   where ur.user_user_id = i_user_id
                         and ss.subsystem_name = i_subsystem_name),
                 (select group_concat(concat(r.role_id, ':', a.actn_id)
                                        order by r.role_id, a.actn_id
                         )
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
                           on ga.grop_grop_id = g.grop_id
                              and g.ssys_ssys_id = ss.ssys_id
                         join
                           actions a
                         on a.ssys_ssys_id = ss.ssys_id
                            and ga.actn_actn_id = a.actn_id
                   where ur.user_user_id = i_user_id
                         and ss.subsystem_name = i_subsystem_name),
                 i_sso_session_id
            from subsystems
           where subsystem_name = i_subsystem_name;

        set v_sess_id = last_insert_id();

        select
            tu.username
            ,ta.*
            ,tu.otp_type_id
            ,tu.otp_code
          from
            (
                select u.user_name as username
                       , otp.otp_type_id
                       , otp.otp_code
                   from users u
                     left join otp_types otp on u.otp_otp_type_id=otp.otp_type_id
                    where user_id = i_user_id
            ) tu
            left join
            (
            select v_sess_id session_id,
                   r.role_name role_role_name,
                   r.principal_name role_principal_name,
                   ss.callback_information role_callback_information,
                   a.action_name role_action_action_name,
                   a.log_action role_action_log_action
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
             where ur.user_user_id = i_user_id and ss.subsystem_name = i_subsystem_name
            union
            select v_sess_id session_id,
                   r.role_name role_role_name,
                   r.principal_name role_principal_name,
                   ss.callback_information role_callback_information,
                   a.action_name role_action_action_name,
                   a.log_action role_action_log_action
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
             where ur.user_user_id = i_user_id and ss.subsystem_name = i_subsystem_name
            ) ta
            on true;
    else
        -- temp password, return only one action

        insert into sessions
              (
                 start_date, user_user_id, ssys_ssys_id, callback_information, role_action_list, action_list, ssos_ssos_id
              )
          select now(),
                 i_user_id,
                 ssys_id,
                 callback_information,
                 null,null,
                 i_sso_session_id
            from subsystems
           where subsystem_name = i_subsystem_name;

        set v_sess_id = last_insert_id();


        select v_user_name username,
               v_sess_id session_id,
               r.role_name role_role_name,
               r.principal_name role_principal_name,
               ss.callback_information role_callback_information,
               'action_temp_password' as  role_action_action_name,
               'Y' as role_action_log_action
               ,v_otp_type_id as otp_type_id
               ,v_otp_code as otp_code
          from           user_roles ur
                       join
                         roles r
                       on r.role_id = ur.role_role_id
                     join
                       subsystems ss
                     on r.ssys_ssys_id = ss.ssys_id
         where ur.user_user_id = i_user_id and ss.subsystem_name = i_subsystem_name;

    end if;
  
  end
;
$$
delimiter ;
