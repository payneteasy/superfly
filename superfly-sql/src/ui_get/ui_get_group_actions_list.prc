drop procedure if exists ui_get_group_actions_list;
delimiter $$
create procedure ui_get_group_actions_list(i_start_from int(10),
                                           i_records_count int(10),
                                           i_order_field_number int(10),
                                           i_order_type varchar(4),
                                           i_grop_id int(10)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select g.grop_id, g.group_name, ss.subsystem_name, a.actn_id, a.action_name ',
                 '  from       groups g ',
                 '           join ',
                 '             subsystems ss ',
                 '           on g.ssys_ssys_id = ss.ssys_id ',
                 '         left join ',
                 '           group_actions ga ',
                 '         on ga.grop_grop_id = g.grop_id ',
                 '       left join ',
                 '         actions a ',
                 '       on ga.actn_actn_id = a.actn_id ',
                 ' where grop_id = ',
                 i_grop_id
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
call save_routine_information('ui_get_group_actions_list',
                              concat_ws(',',
                                        'grop_id int',
                                        'group_name varchar',
                                        'subsystem_name varchar',
                                        'actn_id int',
                                        'action_name varchar'
                              )
     );