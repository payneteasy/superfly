drop procedure if exists get_user_for_description;
delimiter $$
create procedure get_user_for_description(i_username varchar(32))
 main_sql:
  begin
    select user_id
         , user_name username
         , email
         , public_key
    from users 
      where user_name = i_username;
  end                                                    
$$
delimiter ;
call save_routine_information('get_user_for_description',
                              concat_ws(',',
                                        'user_id int',
                                        'username varchar',
                                        'email varchar',
										'public_key varchar'
                              )
     );