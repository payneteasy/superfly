drop procedure if exists ui_get_user_role_actions;
delimiter $$
create procedure ui_get_user_role_actions(i_user_id int(10),
                                          i_ssys_list text,
                                          i_action_name varchar(100),
                                          i_role_name varchar(100)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select u.user_id, ',
                 '       u.user_name, ',
                 '       ss.subsystem_name role_subsystem_name, ',
                 '       r.role_id, ',
                 '       r.role_name, ',
                 '       t.ract_id role_action_ract_id, ',
                 '       t.actn_id role_action_id, ',
                 '       t.action_name role_action_name ',
                 '  from         users u ',
                 '             left join ',
                 '               user_roles ur ',
                 '             on ur.user_user_id = u.user_id ',
                 '           left join ',
                 '             roles r ',
                 '           on ur.role_role_id = r.role_id ',
                 '              and r.role_name like "%',
                 coalesce(i_role_name, ''),
                 '%" ',
                 '         left join ',
                 '           subsystems ss ',
                 '         on r.ssys_ssys_id = ss.ssys_id ',
                 if(i_ssys_list is null,
                    '',
                    concat(' and ssys_id in (', i_ssys_list, ')')
                 ),
                 '       left join ',
                 '         (select ra.ract_id, ra.role_role_id, a.actn_id, a.action_name, a.ssys_ssys_id ',
                 '                 from     role_actions ra ',
                 '                         join ',
                 '                           actions a ',
                 '                         on     a.actn_id = ra.actn_actn_id ',
                 '                            and a.action_name like "%',
                 coalesce(i_action_name, ''),
                 '%" ',
                 '                      join ',
                 '                        user_role_actions ura ',
                 '                      on     ura.ract_ract_id = ra.ract_id ',
                 '                         and ura.user_user_id = ',
                 i_user_id,
                 '         ) t' '       on     ur.role_role_id = t.role_role_id ',
                 '          and r.ssys_ssys_id = t.ssys_ssys_id' ' where u.user_id = ',
                 i_user_id,
                 ' order by ss.subsystem_name, r.role_name, t.action_name '
          );
    set @v_ddl_statement   = v_sql_core;
    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
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