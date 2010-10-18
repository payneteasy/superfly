drop procedure if exists get_user_password_history;
delimiter $$
create procedure get_user_password_history(i_user_name varchar(32))
 main_sql:
  begin

   select  uh.user_password
           ,uh.salt
      from user_history uh
      inner join users u on u.user_id=uh.user_user_id and u.user_name=i_user_name
      where now() between uh.start_date and date_sub(uh.end_date, interval 1 second)
      order by number_history desc;

  end
;
$$
delimiter ;
call save_routine_information('get_user_password_history',
                              concat_ws(',',
                                        'user_password varchar',
                                        'salt varchar'
                              )
     );