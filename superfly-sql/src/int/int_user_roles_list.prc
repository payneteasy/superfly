drop procedure if exists int_user_roles_list;
delimiter $$
create procedure int_user_roles_list(i_start_from int(10),
                                     i_records_count int(10),
                                     i_order_field_number int(10),
                                     i_order_type varchar(4),
                                     i_user_id int(10),
                                     i_mapping_status varchar(1)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select u.user_id, u.user_name, ss.subsystem_name, r.role_id, r.role_name, if(ur.urol_id is null, "U", "M") mapping_status ',
                 '  from       users u ',
                 '           join ',
                 '             roles r ',
                 '         join ',
                 '           subsystems ss ',
                 '         on ss.ssys_id = r.ssys_ssys_id ',
                 '       left join ',
                 '         user_roles ur ',
                 '       on ur.user_user_id = u.user_id and ur.role_role_id = r.role_id ',
                 ' where u.user_id = ',
                 i_user_id,
                 '       and(   (? = "M" and ur.urol_id is not null) ',
                 '           or(? = "U" and ur.urol_id is null) ',
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