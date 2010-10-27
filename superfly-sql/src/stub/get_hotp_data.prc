drop procedure if exists get_hotp_data;
delimiter $$
create procedure get_hotp_data(i_username varchar(32))
 main_sql:
  begin
    select user_id, user_name username, hotp_salt salt, hotp_counter counter from users where user_name = i_username;
  end
$$
delimiter ;
call save_routine_information('get_hotp_data',
                              concat_ws(',',
                                        'user_id int',
                                        'username varchar',
                                        'salt varchar',
                                        'counter int'
                              )
     );