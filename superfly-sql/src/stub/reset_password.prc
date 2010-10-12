drop procedure if exists reset_password;
delimiter $$
create procedure reset_password(i_user_id int(10),
                                i_user_password varchar(128)
                               )
main_sql:
  begin
    update users u set u.user_password = i_user_password, u.is_password_temp = 'Y' where u.user_id = i_user_id;
    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('reset_password', concat_ws(',', 'status varchar', 'error_message varchar'));

