drop procedure if exists authenticate;
delimiter $$
create procedure authenticate(i_user_name varchar(32),
                              i_user_password text,
                              i_subsystem_name varchar(32),
                              i_ip_address varchar(15),
                              i_session_info text
)
 main_sql:
  begin
    declare v_user_id   int(10);

    set v_user_id = int_check_user_password(i_user_name, i_user_password, i_ip_address, i_session_info);

    if v_user_id is null then
      commit; -- to save unauthorized_access INSERT

      select null
        from dual
       where false;

      leave main_sql;
    end if;

    call int_get_user_actions(
        v_user_id,
        i_subsystem_name,
        i_ip_address,
        i_session_info,
        'N',
        null
    );
  
  end
;
$$
delimiter ;
call save_routine_information('authenticate',
                              concat_ws(',',
                                        'username varchar',
                                        'session_id int',
                                        'role_role_name varchar',
                                        'role_principal_name varchar',
                                        'role_callback_information varchar',
                                        'role_action_action_name varchar',
                                        'role_action_log_action varchar'
                              )
     );