drop procedure if exists change_temp_password;
delimiter $$
create procedure change_temp_password(i_user_name varchar(32), 
                                      i_user_password varchar(128) 
                                     )
main_sql:
  begin
    update users u set u.user_password = i_user_password, u.is_password_temp = 'N' where u.user_name = i_user_name;
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('change_temp_password', concat_ws(',', 'status varchar', 'error_message varchar'));

