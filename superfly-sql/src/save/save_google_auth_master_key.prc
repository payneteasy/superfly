drop procedure if exists save_google_auth_master_key;
delimiter $$
create procedure save_google_auth_master_key(i_user_name varchar(32),i_secret_key varchar(64))
 main_sql:
  begin
    update users set master_key=i_secret_key where user_name = i_user_name;
  end
$$
delimiter ;
