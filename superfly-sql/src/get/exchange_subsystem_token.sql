drop procedure if exists exchange_subsystem_token;
delimiter $$
create procedure exchange_subsystem_token(i_subsystem_token varchar(64))
 main_sql:
  begin
    declare v_user_id   int(10);

    select u.user_id
      into v_user_id
      from subsystem_tokens st
        join sso_sessions ss on st.ssos_ssos_id = ss.ssos_id
        join users u on ss.user_user_id = u.user_id
     where     st.token = i_subsystem_token;

    if v_user_id is null then
      commit; -- to save unauthorized_access INSERT

      select null
        from dual
       where false;

      leave main_sql;
    end if;

    delete from subsystem_tokens where token = i_subsystem_token;

    call int_get_user_actions(
        v_user_id,
        i_subsystem_name,
        i_ip_address,
        i_session_info,
        'Y'
    );
  
  end
;
$$
delimiter ;
call save_routine_information('exchange_subsystem_token',
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