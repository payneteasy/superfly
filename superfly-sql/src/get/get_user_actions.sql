drop procedure if exists get_user_actions;
delimiter $$
create procedure get_user_actions(i_user_name varchar(32),
                                  i_subsystem_name varchar(32)
)
 main_sql:
  begin
    declare v_user_id   int(10);

    select user_id
      into v_user_id
      from users u
     where     u.user_name = i_user_name;

    if v_user_id is null then
      select null
        from dual
       where false;

      leave main_sql;
    end if;

    call int_get_user_actions(
        v_user_id,
        i_subsystem_name,
        null,
        null,
        'Y',
        null
    );
  
  end
;
$$
delimiter ;
call save_routine_information('get_user_actions',
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