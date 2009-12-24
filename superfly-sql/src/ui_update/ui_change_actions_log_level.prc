drop procedure if exists ui_change_actions_log_level;
delimiter $$
create procedure ui_change_actions_log_level(i_actn_list_log_on text,
                                             i_actn_list_log_off text
)
 main_sql:
  begin
    declare v_sql_core   text;

    set v_sql_core   =
          concat('update actions ',
                 '   set log_action    = "Y" ',
                 ' where actn_id in (',
                 coalesce(i_actn_list_log_on, '-1'),
                 ') '
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;

    set v_sql_core   =
          concat('update actions ',
                 '   set log_action    = "N" ',
                 ' where actn_id in (',
                 coalesce(i_actn_list_log_off, '-1'),
                 ') '
          );

    set @v_ddl_statement   = v_sql_core;

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_change_actions_log_level',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );