drop procedure if exists ui_filter_subsystems;
delimiter $$
create procedure ui_filter_subsystems()
 main_sql:
  begin
    select ssys_id, subsystem_name from subsystems;
  end
$$
delimiter ;
call save_routine_information('ui_filter_subsystems',
                              concat_ws(',',
                                        'ssys_id int',
                                        'subsystem_name varchar'
                              )
     );