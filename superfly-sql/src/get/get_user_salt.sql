drop procedure if exists get_user_salt;
delimiter $$
create procedure get_user_salt(i_user_name varchar(32))
 main_sql:
  begin

    declare v_salt varchar(64) default null;

    select salt into v_salt
      from users u
     where u.user_name = i_user_name;

    select v_salt as salt; 

  end
;
$$
delimiter ;
call save_routine_information('get_user_salt',
                              concat_ws(',',
                                        'salt varchar'
                              )
     );