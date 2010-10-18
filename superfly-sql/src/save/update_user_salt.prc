drop procedure if exists update_user_salt;
delimiter $$
create procedure update_user_salt(i_user_name varchar(32),i_salt varchar(64))
 main_sql:
  begin
    update users set salt=i_salt where user_name=i_user_name;
  end
$$
delimiter ;
