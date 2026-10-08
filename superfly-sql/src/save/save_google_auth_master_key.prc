drop procedure if exists save_google_auth_master_key;
delimiter $$
create procedure save_google_auth_master_key(i_user_name varchar(32),i_secret_key varchar(128))
 main_sql:
  begin
    -- an admin reset (null) or a newly set up key outdates a pending key: confirming it later would replace this one
    update users set master_key=i_secret_key, otp_pending_master_key = null where user_name = i_user_name;
  end
$$
delimiter ;
