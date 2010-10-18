drop procedure if exists get_user_salt_by_user_id;
delimiter $$
create procedure get_user_salt_by_user_id(i_user_id int(11))
 main_sql:
  begin

    declare v_salt varchar(64) default null;

    select salt into v_salt
      from users u
     where u.user_id = i_user_id;

    select v_salt as salt; 

  end
;
$$
delimiter ;
call save_routine_information('get_user_salt_by_user_id',
                              concat_ws(',',
                                        'salt varchar'
                              )
     );