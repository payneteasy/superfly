drop procedure if exists ui_get_users_list_count;
delimiter $$
create procedure ui_get_users_list_count(i_user_name varchar(32),
                                         i_role_id int(10),
                                         i_comp_id int(10)
)
 main_sql:
  begin
    declare v_sql_core            text;
    declare v_search_conditions   text;

    call int_users_list(i_user_name, i_role_id, i_comp_id, v_search_conditions);

    set v_sql_core   =
          concat('select count(1) records_count ',
                 '  from users u ',
                 ' where true ',
                 coalesce(v_search_conditions, '')
          );

    set @v_ddl_statement   = concat(v_sql_core);

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;
call save_routine_information('ui_get_users_list_count',
                              concat_ws(',', 'records_count int')
     );