drop procedure if exists ui_get_actions_list_with_group;
delimiter $$
create procedure ui_get_actions_list_with_group(i_start_from int(10),
                                                i_records_count int(10),
                                                i_order_field_number int(10),
                                                i_order_type varchar(4),
                                                i_action_name varchar(128),
                                                i_action_description varchar(512),
                                                i_ssys_list text)
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
          concat('select a.actn_id, a.action_name, ',
                 '       a.action_description, ',
                 '       a.log_action, ',
                 '       ss.ssys_id, ',
                 '       ss.subsystem_name, ',
                 '       g.grop_id, ',
                 '       g.group_name ',
                 '  from actions a, subsystems ss, group_actions ga, groups g ',
                 ' where a.ssys_ssys_id = ss.ssys_id ',
                 '   and a.actn_id = ga.actn_actn_id ',
                 '   and g.grop_id = ga.grop_grop_id ',
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
call save_routine_information('ui_get_actions_list_with_group',
                              concat_ws(',',
                                        'actn_id int',
                                        'action_name varchar',
                                        'action_description varchar',
                                        'log_action varchar',
                                        'ssys_id int',
                                        'subsystem_name varchar',
                                        'grop_id int',
                                        'group_name varchar'
                              )
     );