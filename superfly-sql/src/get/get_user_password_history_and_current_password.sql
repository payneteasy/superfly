drop procedure if exists get_user_password_history_and_current_password;
delimiter $$
create procedure get_user_password_history_and_current_password(i_user_name varchar(32))
 main_sql:
  begin

   -- Newest first: the current password (temporary ones included: it is known to the admin and must not be
   -- reusable) followed by the history. ORDER BY inside a UNION branch is ignored by MySQL, hence the outer one.
   -- The newest history row stands for the current password and is skipped when the current one is listed
   -- (compared by number, not by hash: a legacy hash is replaced by pbkdf2 in users at login, history keeps it).
   -- A temporary current password has no history row of its own, so nothing is skipped then: it is always first.
   select t.user_password, t.salt
     from (
           select u.user_password, u.salt, 2147483647 as sort_key
             from users u
            where u.user_name = i_user_name
           union all
           select uh.user_password, uh.salt, uh.number_history as sort_key
             from user_history uh
                  inner join users u on u.user_id = uh.user_user_id
            where u.user_name = i_user_name
              and (u.is_password_temp <> 'N'
                   or uh.number_history < (select max(uh2.number_history)
                                             from user_history uh2
                                            where uh2.user_user_id = u.user_id))
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