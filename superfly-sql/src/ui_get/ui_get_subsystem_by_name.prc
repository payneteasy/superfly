drop procedure if exists ui_get_subsystem_by_name;
delimiter $$
create procedure ui_get_subsystem_by_name(i_subsystem_name varchar(32))
 main_sql:
  begin
    select ss.ssys_id,
           ss.subsystem_name,
           ss.callback_information,
           ss.fixed,
           ss.allow_list_users
      from subsystems ss
     where ss.subsystem_name = i_subsystem_name;
  end
$$
delimiter ;
call save_routine_information('ui_get_subsystem_by_name',
                              concat_ws(',',
                                        'ssys_id int',
                                        'subsystem_name varchar',
                                        'callback_information varchar',
                                        'fixed varchar',
                                        'allow_list_users varchar'
                              )
     );