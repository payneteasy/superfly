drop procedure if exists int_link_user_roles;
delimiter $$
create procedure int_link_user_roles(i_user_id int(10), i_roles_list text)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('insert into user_roles ',
                 '      ( ',
                 '         user_user_id, role_role_id ',
                 '      ) ',
                 '  select u.user_id, r.role_id ',
                 '    from     users u ',
                 '           join ',
                 '             roles r ',
                 '           on r.role_id in (',
                 coalesce(i_roles_list, '-1'),
                 ') ',
                 '         left join ',
                 '           user_roles ur ',
                 '         on r.role_id = ur.role_role_id and ur.user_user_id = u.user_id ',
                 '   where ur.urol_id is null and u.user_id = ',
                 i_user_id
          );


    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;