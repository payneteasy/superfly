drop procedure if exists ui_update_user_is_otp_optional_value;
delimiter $$
create procedure ui_update_user_is_otp_optional_value(i_user_name varchar(32), i_is_otp_optional varchar(1))
 main_sql:
  begin
    update users set is_otp_optional = i_is_otp_optional where user_name = i_user_name;
  end
$$
delimiter ;
