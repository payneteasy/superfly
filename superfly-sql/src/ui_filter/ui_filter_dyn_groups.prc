drop procedure if exists ui_filter_dyn_groups;
delimiter $$
create procedure ui_filter_dyn_groups(i_ssys_list text,
                                      i_group_name varchar(100),
                                      i_start_from int(10),
                                      i_records_count int(10)
)
 main_sql:
  begin
    declare v_sql_core     text;
    declare v_conditions   text default '';

    if i_ssys_list is not null then
      set v_conditions   =
            concat(v_conditions, ' and g.ssys_ssys_id in (', i_ssys_list, ')');
    end if;

    set v_sql_core   =
          concat('select g.grop_id, g.group_name, ss.subsystem_name ',
                 '  from groups g, subsystems ss ',
                 ' where g.ssys_ssys_id = ss.ssys_id ',
                 v_conditions,
                 '       and g.group_name like "',
                 coalesce(i_group_name, ''),
                 '%" ',
                 'order by g.group_name ',
                 ' limit ',
                 i_start_from,
                 ', ',
                 i_records_count
          );

    set @sv_ddl_statement   = v_sql_core;

    prepare v_stmt from @sv_ddl_statement;
    execute v_stmt;

    deallocate prepare v_stmt;
  end
$$
delimiter ;
call save_routine_information('ui_filter_dyn_groups',
                              concat_ws(',',
                                        'grop_id int',
                                        'group_name varchar',
                                        'subsystem_name varchar'
                              )
     );