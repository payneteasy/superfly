drop procedure if exists ui_get_group;
delimiter $$
create procedure ui_get_group(i_grop_id int(10))
 main_sql:
  begin
    select g.grop_id,
           g.group_name,
           ss.ssys_id subsystem_ssys_id,
           ss.subsystem_name
      from groups g, subsystems ss
     where g.ssys_ssys_id = ss.ssys_id and g.grop_id = i_grop_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_group',
                              concat_ws(',',
                                        'grop_id int',
                                        'group_name varchar',
                                        'subsystem_ssys_id int',
                                        'subsystem_name varchar'
                              )
     );