drop procedure if exists ui_get_subsystem;
delimiter $$
create procedure ui_get_subsystem(i_ssys_id int(10))
 main_sql:
  begin
    select ss.ssys_id, ss.subsystem_name, ss.callback_information, ss.fixed
      from subsystems ss
     where ss.ssys_id = i_ssys_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_subsystem',
                              concat_ws(',',
                                        'ssys_id int',
                                        'subsystem_name varchar',
                                        'callback_information varchar',
                                        'fixed varchar'
                              )
     );