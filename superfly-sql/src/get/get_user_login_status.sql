drop procedure if exists get_user_login_status;
delimiter $$
create procedure get_user_login_status(
        i_user_name varchar(32),
        i_user_password text,
        i_subsystem_name varchar(32)
)
 main_sql:
  begin

    declare v_user_id   int(10);
    declare v_temp      varchar(1);
    declare v_result    varchar(1);

     -- TODO: user IP, session info
    set v_user_id = int_check_user_password(i_user_name, i_user_password, null, null);
    if v_user_id is null then
        commit; -- to save unauthorized_access INSERT
        set v_result = 'N';
    else
        select is_password_temp
          into v_temp
          from users u
         where     u.user_id = v_user_id;
        if is_password_temp = 'Y' then
            set v_result = 'T';
        else
            set v_result = 'Y';
        end if;
    end if;

    select v_result status;
  end
;
$$
delimiter ;
call save_routine_information('get_user_login_status',
                              concat_ws(',',
                                        'status varchar'
                              )
     );