drop procedure if exists save_otp_pending_master_key;
delimiter $$
create procedure save_otp_pending_master_key(i_user_name varchar(32), i_secret_key varchar(128))
 main_sql:
  begin
    update users set otp_pending_master_key = i_secret_key where user_name = i_user_name;
  end
$$
delimiter ;
