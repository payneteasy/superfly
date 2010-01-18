drop procedure if exists int_user_roles_list_count;
delimiter $$
create procedure int_user_roles_list_count(i_user_id int(10),
										   i_ssys_list text,
                                           i_mapping_status varchar(1)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   = concat(
	    'select count(1) records_count ',
	    '  from       users u ',
	    '           join ',
	    '             roles r ',
	    '         join ',
	    '           subsystems ss ',
	    '         on ss.ssys_id = r.ssys_ssys_id ',
                 if(i_ssys_list is null or i_ssys_list = '',
                    '',
                    concat(' and ssys_id in (', i_ssys_list, ')')
                 ),
	    '       left join ',
	    '         user_roles ur ',
	    '       on ur.user_user_id = u.user_id and ur.role_role_id = r.role_id ',
	    ' where u.user_id = ? ',
	    '       and(   (? = "M" and ur.urol_id is not null) ',
	    '           or(? = "U" and ur.urol_id is null) ',
	    '           or coalesce(?, "A") = "A") '
    );
    
    set @v_ddl_statement   =
          concat(v_sql_core
          );
    set @user_id   = i_user_id;
    set @mapping_status   = i_mapping_status;
    prepare v_stmt from @v_ddl_statement;
    execute v_stmt using @user_id, @mapping_status, @mapping_status, @mapping_status;

    deallocate prepare v_stmt;
  end
$$
delimiter ;