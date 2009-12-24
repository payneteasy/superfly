drop procedure if exists ui_get_groups_list;
delimiter $$
create procedure ui_get_groups_list(i_start_from int(10),
                                    i_records_count int(10),
                                    i_order_field_number int(10),
                                    i_order_type varchar(4),
                                    i_group_name varchar(32),
                                    i_ssys_list text
)
 main_sql:
  begin
    declare v_sql_core            text;
    declare v_search_conditions   text;

    call int_groups_list(i_group_name, i_ssys_list, v_search_conditions);

    set v_sql_core   =
          concat('   select g.grop_id, g.group_name, ss.ssys_id, ss.subsystem_name ',
                 '      from subsystems ss, groups g ',
                 '     where g.ssys_ssys_id = ss.ssys_id',
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
call save_routine_information('ui_get_groups_list',
                              concat_ws(',',
                                        'grop_id int',
                                        'group_name varchar',
                                        'ssys_id int',
                                        'subsystem_name varchar'
                              )
     );