drop procedure if exists ui_filter_roles;
delimiter $$
create procedure ui_filter_roles()
 main_sql:
  begin
    select role_id, role_name, subsystem_name from roles join subsystems on ssys_ssys_id = ssys_id;
  end
$$
delimiter ;
call save_routine_information('ui_filter_roles',
                              concat_ws(',',
                                        'role_id int',
                                        'role_name varchar',
                                        'subsystem_name varchar'
                              )
     );