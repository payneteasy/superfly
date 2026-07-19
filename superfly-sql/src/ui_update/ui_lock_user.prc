drop procedure if exists ui_lock_user;
delimiter $$
create procedure ui_lock_user(i_user_id int(10))
 main_sql:
  begin
    declare v_user_name   varchar(32);
    declare v_subsys_id   int;
    declare v_count       int default 0;
    declare done          int default 0;
    declare cur cursor for
        select distinct r.ssys_ssys_id
          from user_roles ur
          join roles r on r.role_id = ur.role_role_id
         where ur.user_user_id = i_user_id
           and r.ssys_ssys_id is not null;
    declare continue handler for not found set done = 1;

    update users
       set is_account_locked = 'Y'
     where user_id = i_user_id;

    select user_name into v_user_name
      from users where user_id = i_user_id;

    open cur;
    read_loop: loop
        fetch cur into v_subsys_id;
        if done then leave read_loop; end if;
        call int_create_event('ACCOUNT_LOCK', v_user_name, v_subsys_id);
        set v_count = v_count + 1;
    end loop;
    close cur;

    if v_count = 0 then
        call int_create_event('ACCOUNT_LOCK', v_user_name, null);
    end if;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_lock_user',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );
