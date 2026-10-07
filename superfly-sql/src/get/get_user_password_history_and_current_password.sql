drop procedure if exists get_user_password_history_and_current_password;
delimiter $$
create procedure get_user_password_history_and_current_password(i_user_name varchar(32))
 main_sql:
  begin

   -- Newest first: the current password (unless it is temporary, i.e. set by an admin reset and not by the user)
   -- followed by the history. ORDER BY inside a UNION branch is ignored by MySQL, hence the outer one.
   -- A history row equal to the current password is skipped: it is the same password (or the temporary one
   -- the user was created with), counted once.
   select t.user_password, t.salt
     from (
           select u.user_password, u.salt, 2147483647 as sort_key
             from users u
            where u.user_name = i_user_name
              and u.is_password_temp = 'N'
           union all
           select uh.user_password, uh.salt, uh.number_history as sort_key
             from user_history uh
                  inner join users u on u.user_id = uh.user_user_id
            where u.user_name = i_user_name
              and not (uh.user_password <=> u.user_password and uh.salt <=> u.salt)
          ) t
    order by t.sort_key desc;

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