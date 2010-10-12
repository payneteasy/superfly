drop procedure if exists ui_suspend_user;
delimiter $$
create procedure ui_suspend_user(i_user_id int(11))
 main_sql:
  begin
  	update users set is_account_locked = 'Y', is_account_suspended = 'Y'
  		where user_id = i_user_id;
  		
  	select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_suspend_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );