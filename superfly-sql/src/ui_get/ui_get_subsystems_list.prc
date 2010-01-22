drop procedure if exists ui_get_subsystems_list;
delimiter $$
create procedure ui_get_subsystems_list()
 main_sql:
  begin
    select ssys_id,
           subsystem_name,
           callback_information,
           fixed,
           allow_list_users
      from subsystems;
  end
$$
delimiter ;
call save_routine_information('ui_get_subsystems_list',
                              concat_ws(',',
                                        'ssys_id int',
                                        'subsystem_name varchar',
                                        'callback_information varchar',
                                        'fixed varchar',
                                        'allow_list_users varchar'
                              )
     );