drop procedure if exists ui_get_role;
delimiter $$
create procedure ui_get_role(i_role_id int(10))
 main_sql:
  begin
    select r.role_id,
           r.role_name,
           r.principal_name,
           ss.ssys_id,
           ss.subsystem_name
      from roles r, subsystems ss
     where r.ssys_ssys_id = ss.ssys_id and r.role_id = i_role_id;
  end
$$
delimiter ;
call save_routine_information('ui_get_role',
                              concat_ws(',',
                                        'role_id int',
                                        'role_name varchar',
                                        'principal_name varchar',
                                        'log_action varchar',
                                        'ssys_id int',
                                        'subsystem_name varchar'
                              )
     );