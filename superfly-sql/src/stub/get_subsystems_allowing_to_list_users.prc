drop procedure if exists get_subsystems_allowing_to_list_users;
delimiter $$
create procedure get_subsystems_allowing_to_list_users()
 main_sql:
  begin
    select ssys_id, callback_information, send_callbacks from subsystems where allow_list_users='Y';
  end
$$
delimiter ;
call save_routine_information('get_subsystems_allowing_to_list_users',
                              concat_ws(',',
                                        'ssys_id int',
                                        'callback_information varchar',
                                        'send_callbacks varchar'
                              )
     );