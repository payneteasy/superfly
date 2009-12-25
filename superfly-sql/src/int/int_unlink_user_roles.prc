drop procedure if exists int_unlink_user_roles;
delimiter $$
create procedure int_unlink_user_roles(i_user_id int(10), i_roles_list text)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('delete from user_roles ',
                 ' where user_user_id = ',
                 i_user_id,
                 '       and role_role_id in (',
                 coalesce(i_roles_list, '-1'),
                 ')'
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;

    delete ura
      from     user_role_actions ura
             join
               role_actions ra
             on ura.ract_ract_id = ra.ract_id
           left join
             user_roles ur
           on ur.role_role_id = ra.role_role_id
              and ura.user_user_id = ur.user_user_id
     where ura.user_user_id = i_user_id and ur.urol_id is null;
  end
$$
delimiter ;