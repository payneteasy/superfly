drop procedure if exists ui_get_roles_list_count;
delimiter $$
create procedure ui_get_roles_list_count(i_role_name varchar(32),
                                         i_ssys_list text
)
 main_sql:
  begin
    declare v_sql_core            text;
    declare v_search_conditions   text;

    call int_roles_list(i_role_name, i_ssys_list, v_search_conditions);

    set v_sql_core   =
          concat('select count(1) records_count ',
                 '  from roles r ',
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
call save_routine_information('ui_get_roles_list_count',
                              concat_ws(',', 'records_count int')
     );