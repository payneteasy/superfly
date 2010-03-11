drop procedure if exists ui_delete_user;
delimiter $$
create procedure ui_delete_user(i_user_id int(10))
 main_sql:
  begin
	call ui_lock_user(i_user_id);
  end
$$
delimiter ;
call save_routine_information('ui_delete_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );