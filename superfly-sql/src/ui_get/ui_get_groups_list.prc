drop procedure if exists ui_get_groups_list;
delimiter $$
create procedure ui_get_groups_list(i_ssys_list text)
 main_sql:
  begin
    select g.grop_id, g.group_name, ss.ssys_id, ss.subsystem_name
      from subsystems ss, groups g
     where g.ssys_ssys_id = ss.ssys_id
           and instr(concat(',', coalesce(i_ssys_list, g.ssys_ssys_id), ','),
                     concat(',', g.ssys_ssys_id, ',')
              ) > 0;
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