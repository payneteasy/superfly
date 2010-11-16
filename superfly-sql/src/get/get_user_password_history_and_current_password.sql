drop procedure if exists get_user_password_history_and_current_password;
delimiter $$
create procedure get_user_password_history_and_current_password(i_user_name varchar(32))
 main_sql:
  begin

   (select user_password, salt from users where user_name = i_user_name)
   union
   (
   select  uh.user_password
           ,uh.salt
      from user_history uh
      inner join users u on u.user_id=uh.user_user_id and u.user_name=i_user_name
      order by number_history desc
   );

  end
;
$$
delimiter ;
call save_routine_information('get_user_password_history_and_current_password',
                              concat_ws(',',
                                        'user_password varchar',
                                        'salt varchar'
                              )
     );