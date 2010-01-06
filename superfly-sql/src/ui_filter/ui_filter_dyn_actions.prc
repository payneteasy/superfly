drop procedure if exists ui_filter_dyn_actions;
delimiter $$
create procedure ui_filter_dyn_actions(i_ssys_list text,
                                       i_action_name varchar(100),
                                       i_start_from int(10),
                                       i_records_count int(10)
)
 main_sql:
  begin
    declare v_sql_core     text;
    declare v_conditions   text default '';

    if i_ssys_list is not null then
      set v_conditions   =
            concat(v_conditions, ' and a.ssys_ssys_id in (', i_ssys_list, ')');
    end if;

    set v_sql_core   =
          concat('select a.actn_id, a.action_name, ss.subsystem_name ',
                 '  from actions a, subsystems ss ',
                 ' where a.ssys_ssys_id = ss.ssys_id ',
                 v_conditions,
                 '       and a.action_name like "',
                 coalesce(i_action_name, ''),
                 '%" ',
                 'order by a.action_name ',
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
call save_routine_information('ui_filter_dyn_actions',
                              concat_ws(',',
                                        'actn_id int',
                                        'action_name varchar',
                                        'subsystem_name varchar'
                              )
     );