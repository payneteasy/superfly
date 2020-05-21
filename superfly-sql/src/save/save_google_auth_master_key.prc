drop procedure if exists save_google_auth_master_key;
delimiter $$
create procedure save_google_auth_master_key(i_user_id int(10),i_secret_key varchar(64))
 main_sql:
  begin
    -- todo:
  end
$$
delimiter ;
