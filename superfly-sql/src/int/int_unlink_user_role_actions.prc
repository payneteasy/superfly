drop procedure if exists int_unlink_user_role_actions;
delimiter $$
create procedure int_unlink_user_role_actions(i_user_id int(10),
                                              i_role_actions_list text
)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('delete from user_role_actions ',
                 ' where user_user_id = ',
                 i_user_id,
                 '       and ract_ract_id in (',
                 coalesce(i_role_actions_list, '-1'),
                 ')'
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;