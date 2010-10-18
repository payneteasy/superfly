drop procedure if exists get_flag_temp_password;
delimiter $$
create procedure get_flag_temp_password(i_user_name varchar(32))
main_sql:
  begin
     select u.is_password_temp from users u where u.user_name = i_user_name;
  end
$$
delimiter ;
call save_routine_information('get_flag_temp_password', concat_ws(',', 'is_password_temp varchar'));

