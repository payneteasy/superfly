drop procedure if exists ui_get_role_groups_list;
delimiter $$
create procedure ui_get_role_groups_list(i_start_from int(10),
                                         i_records_count int(10),
                                         i_order_field_number int(10),
                                         i_order_type varchar(4),
                                         i_role_id int(10)
)
 main_sql:
  begin
    declare v_sql_core   text;
    set v_sql_core   =
          concat('select r.role_id, r.role_name, ss.subsystem_name, g.grop_id, g.group_name ',
                 '  from       roles r ',
                 '           join ',
                 '             subsystems ss ',
                 '           on r.ssys_ssys_id = ss.ssys_id ',
                 '         left join ',
                 '           role_groups rg ',
                 '         on rg.role_role_id = r.role_id ',
                 '       left join ',
                 '         groups g ',
                 '       on rg.grop_grop_id = g.grop_id ',
                 ' where role_id = ',
                 i_role_id
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
call save_routine_information('ui_get_role_groups_list',
                              concat_ws(',',
                                        'role_id int',
                                        'role_name varchar',
                                        'subsystem_name varchar',
                                        'grop_id int',
                                        'group_name varchar'
                              )
     );