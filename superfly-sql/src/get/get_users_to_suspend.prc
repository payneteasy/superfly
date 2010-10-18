drop procedure if exists get_users_to_suspend;
delimiter $$
create procedure get_users_to_suspend(i_days int(10))
 main_sql:
  begin
  	select user_id, user_name from users
  		where date_add(coalesce(last_login_date, create_date), interval i_days day) < now();
  end
$$
delimiter ;
call save_routine_information('get_users_to_suspend',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar'
                              )
     );