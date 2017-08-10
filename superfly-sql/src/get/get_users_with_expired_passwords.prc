drop procedure if exists get_users_with_expired_passwords;
delimiter $$
create procedure get_users_with_expired_passwords(i_days int(10))
 main_sql:
  begin
    select  distinct user_id
           ,user_name
          from users u
       inner join ( 
        select user_user_id from user_history 
           where now() between start_date and end_date
           and DATEDIFF(now(),start_date)>=i_days ) uh on uh.user_user_id=u.user_id
       where is_password_temp = 'N';

  end
$$
delimiter ;
call save_routine_information('get_users_with_expired_passwords',
                              concat_ws(',',
                                        'user_id int',
                                        'user_name varchar'
                              )
     );