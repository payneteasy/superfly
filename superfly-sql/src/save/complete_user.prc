drop procedure if exists complete_user;
delimiter $$
create procedure complete_user(i_user_name varchar(32))
 main_sql:
  begin
    update users set completed='Y' where user_name=i_user_name;
  end
$$
delimiter ;
