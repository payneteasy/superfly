drop procedure if exists get_user_salt;
delimiter $$
create procedure get_user_salt(i_user_name varchar(32))
 main_sql:
  begin

    select salt 
      from users u
     where u.user_name = i_user_name;

  end
;
$$
delimiter ;
call save_routine_information('get_user_salt',
                              concat_ws(',',
                                        'salt varchar'
                              )
     );