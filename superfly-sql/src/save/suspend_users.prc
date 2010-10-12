drop procedure if exists suspend_users;
delimiter $$
create procedure suspend_users(i_days int(10))
 main_sql:
  begin
  	update users set is_account_locked = 'Y', is_account_suspended = 'Y'
  		where date_add(coalesce(last_login_date, create_date), interval i_days day) < now();
  end
$$
delimiter ;
