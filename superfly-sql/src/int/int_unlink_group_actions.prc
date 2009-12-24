drop procedure if exists int_unlink_group_actions;
delimiter $$
create procedure int_unlink_group_actions(i_grop_id int(10),
                                          i_actions_list text
)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('delete from group_actions ',
                 ' where     grop_grop_id = ',
                 i_grop_id,
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