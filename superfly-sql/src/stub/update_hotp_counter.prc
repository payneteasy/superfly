drop procedure if exists update_hotp_counter;
delimiter $$
create procedure update_hotp_counter(i_username varchar(32), i_value int(11))
 main_sql:
  begin
  	update users set hotp_counter = i_value where user_name = i_username;
  end
$$
delimiter ;
