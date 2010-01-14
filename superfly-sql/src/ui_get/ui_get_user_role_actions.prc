drop procedure if exists ui_get_user_role_actions;
delimiter $$
create procedure ui_get_user_role_actions(i_user_id int(10),
                                          i_ssys_list text,
                                          i_action_name varchar(100)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select u.user_id, ',
                 '       u.user_name, ',
                 '       ss.subsystem_name, ',
                 '       r.role_id, ',
                 '       r.role_name, ',
                 '       ra.ract_id, ',
                 '       a.actn_id, ',
                 '       a.action_name, ',
                 '       if(ura.urac_id is not null, "M", if(ra.ract_id is not null, "U", null)) mapping_status ',
                 '  from             users u ',
                 '                 join ',
                 '                   user_roles ur ',
                 '                 on ur.user_user_id = u.user_id ',
                 '               join ',
                 '                 roles r ',
                 '               on ur.role_role_id = r.role_id ',
                 '             left join ',
                 '               role_actions ra ',
                 '             on ur.role_role_id = ra.role_role_id ',
                 '           left join ',
                 '             actions a ',
                 '           on r.ssys_ssys_id = a.ssys_ssys_id ',
                 '              and a.actn_id = ra.actn_actn_id ',
                 '              and a.action_name like "%',
                 coalesce(i_action_name, ''),
                 '%" ',
                 '         join ',
                 '           subsystems ss ',
                 '         on r.ssys_ssys_id = ss.ssys_id ',
                 if(i_ssys_list is null,
                    '',
                    concat(' and ssys_id in (', i_ssys_list, ')')
                 ),
                 '       left join ',
                 '         user_role_actions ura ',
                 '       on ura.ract_ract_id = ra.ract_id ',
                 '          and ura.user_user_id = u.user_id ',
                 ' where u.user_id = ',
                 i_user_id
          );
    set @v_ddl_statement   =
          concat(v_sql_core,
                 ' order by ss.subsystem_name, r.role_name, a.action_name'
          );
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
                                        'subsystem_name varchar',
                                        'role_id int',
                                        'role_name varchar',
                                        'ract_id int',
                                        'actn_id int',
                                        'action_name varchar',
                                        'mapping_status varchar'
                              )
     );