drop procedure if exists ui_filter_dyn_roles;
delimiter $$
create procedure ui_filter_dyn_roles(i_ssys_list text,
                                     i_role_name varchar(100),
                                     i_start_from int(10),
                                     i_records_count int(10)
)
 main_sql:
  begin
    declare v_sql_core     text;
    declare v_conditions   text default '';

    if i_ssys_list is not null then
      set v_conditions   =
            concat(v_conditions, ' and r.ssys_ssys_id in (', i_ssys_list, ')');
    end if;

    set v_sql_core   =
          concat('select r.role_id, r.role_name, ss.subsystem_name ',
                 '  from roles r, subsystems ss ',
                 ' where r.ssys_ssys_id = ss.ssys_id ',
                 v_conditions,
                 '       and r.role_name like "',
                 coalesce(i_role_name, ''),
                 '%" ',
                 'order by r.role_name ',
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
call save_routine_information('ui_filter_dyn_roles',
                              concat_ws(',',
                                        'role_id int',
                                        'role_name varchar',
                                        'subsystem_name varchar'
                              )
     );