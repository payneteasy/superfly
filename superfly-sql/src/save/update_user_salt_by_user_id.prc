drop procedure if exists update_user_salt_by_user_id;
delimiter $$
create procedure update_user_salt_by_user_id(i_user_id int(11),i_salt varchar(64))
 main_sql:
  begin
    update users set salt=i_salt where user_id=i_user_id;
  end
$$
delimiter ;
