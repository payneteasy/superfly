drop procedure if exists int_user_actions_list;
delimiter $$
create procedure int_user_actions_list(i_start_from int(10),
                                       i_records_count int(10),
                                       i_order_field_number int(10),
                                       i_order_type varchar(4),
                                       i_user_id int(10),
                                       i_ssys_list text,
                                       i_role_id int(10),
                                       i_mapping_status varchar(1),
                                       i_action_name varchar(100)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select u.user_id, u.user_name, ss.subsystem_name, a.actn_id, a.action_name, ',
                 '       if(ura.urac_id is null, "U", "M") mapping_status, ra.ract_id, r.role_id, ',
                 '       concat(if(ura.urac_id is null, "Can be granted thru role : ", "Granted thru role : "), r.role_name) role_name ',
                 '  from             users u ',
                 '                 join ',
                 '                   user_roles ur ',
                 '                 on ur.user_user_id = u.user_id ',
                 '               join ',
                 '                 roles r ',
                 '               on ur.role_role_id = r.role_id ',
                 '             join ',
                 '               role_actions ra ',
                 '             on ur.role_role_id = ra.role_role_id ',
                 '           join ',
                 '             actions a ',
                 '           on a.actn_id = ra.actn_actn_id ',
                 '              and a.action_name like "%',
                 coalesce(i_action_name, ''),
                 '%" ',
                 '         join ',
                 '           subsystems ss ',
                 '         on a.ssys_ssys_id = ss.ssys_id ',
                 if(i_ssys_list is null,
                    '',
                    concat(' and ssys_id in (', i_ssys_list, ')')
                 ),
                 '       left join ',
                 '         user_role_actions ura ',
                 '       on ura.ract_ract_id = ra.ract_id and ura.user_user_id = u.user_id ',
                 ' where u.user_id = ',
                 i_user_id,
                 if (i_role_id is null,
                     '',
                     concat(' and r.role_id = ', i_role_id, ' ')
                 ),
                 '       and(   (? = "M" and ura.urac_id is not null) ',
                 '           or(? = "U" and ura.urac_id is null) ',
                 '           or coalesce(?, "A") = "A")'
          );
    set @v_ddl_statement   =
          concat(v_sql_core,
                 ' order by ',
                 i_order_field_number,
                 ' ',
                 i_order_type,
                 ' limit ',
                 i_start_from,
                 ', ',
                 i_records_count
          );
    set @mapping_status   = i_mapping_status;
    prepare v_stmt from @v_ddl_statement;
    execute v_stmt using @mapping_status, @mapping_status, @mapping_status;

    deallocate prepare v_stmt;
  end
$$
delimiter ;