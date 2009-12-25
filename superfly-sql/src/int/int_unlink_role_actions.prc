drop procedure if exists int_unlink_role_actions;
delimiter $$
create procedure int_unlink_role_actions(i_role_id int(10), i_actions_list text
)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('delete ura ',
                 '  from     user_role_actions ura ',
                 '         join ',
                 '           role_actions ra ',
                 '         on ura.ract_ract_id = ra.ract_id ',
                 ' where ra.role_role_id = ',
                 i_role_id,
                 '       and ra.actn_actn_id in (',
                 coalesce(i_actions_list, '-1'),
                 ') '
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;

    set v_sql_core   =
          concat('delete from role_actions ',
                 ' where role_role_id = ',
                 i_role_id,
                 '       and actn_actn_id in (',
                 coalesce(i_actions_list, '-1'),
                 ') '
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;