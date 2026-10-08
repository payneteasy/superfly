drop procedure if exists save_google_auth_master_key_if_unchanged;
delimiter $$
create procedure save_google_auth_master_key_if_unchanged(i_user_name varchar(32), i_old_secret_key varchar(128), i_new_secret_key varchar(128))
 main_sql:
  begin
    update users set master_key=i_new_secret_key where user_name = i_user_name and master_key = i_old_secret_key;
  end
$$
delimiter ;
