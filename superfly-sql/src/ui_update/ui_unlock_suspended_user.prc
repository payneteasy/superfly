drop procedure if exists ui_unlock_suspended_user;
delimiter $$
create procedure ui_unlock_suspended_user(i_user_id int(10), i_new_password varchar(255))
 main_sql:
  begin
    update users
       set is_account_locked    = "N", logins_failed=null,
       	is_account_suspended = 'N', user_password = i_new_password,
       	is_password_temp = 'Y'
     where user_id = i_user_id;
     
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_unlock_suspended_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );