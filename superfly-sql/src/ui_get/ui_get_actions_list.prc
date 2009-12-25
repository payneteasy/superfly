drop procedure if exists ui_get_actions_list;
delimiter $$
create procedure ui_get_actions_list(i_start_from int(10),
                                     i_records_count int(10),
                                     i_order_field_number int(10),
                                     i_order_type varchar(4),
                                     i_action_name varchar(128),
                                     i_action_description varchar(512),
                                     i_ssys_list text
)
 main_sql:
  begin
    declare v_sql_core            text;
    declare v_search_conditions   text;
    call int_actions_list(i_action_name,
                          i_action_description,
                          i_ssys_list,
                          v_search_conditions
         );


    set v_sql_core   =
          concat('select a.action_name, ',
                 '       a.action_description, ',
                 '       a.log_action, ',
                 '       ss.subsystem_name ',
                 '  from actions a, subsystems ss ',
                 ' where a.ssys_ssys_id = ss.ssys_id ',
                 coalesce(v_search_conditions, '')
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

    prepare v_stmt from @v_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;
call save_routine_information('ui_get_actions_list',
                              concat_ws(',',
                                        'action_name varchar',
                                        'action_description varchar',
                                        'log_action varchar',
                                        'subsystem_name varchar'
                              )
     );