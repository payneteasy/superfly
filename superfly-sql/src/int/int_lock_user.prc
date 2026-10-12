drop procedure if exists int_lock_user;
delimiter $$
create procedure int_lock_user(i_user_id int(10), out o_locked_now tinyint)
 main_sql:
  begin
    -- returns no result set: the caller answers the client itself, and the client reads only the first
    -- result set of a call, so a result set from here would hide the answer of a calling procedure.
    -- o_locked_now is 1 only for the call that has changed the account from not locked to locked: a repeated
    -- or a concurrent call finds it locked, and then ends no sessions, creates no events and reports nothing.
    -- An out parameter and not a session variable, so that the value does not outlive the call
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

    set o_locked_now = 0;

    update users
       set is_account_locked = 'Y'
     where user_id = i_user_id
       and coalesce(is_account_locked, 'N') <> 'Y';

    if row_count() <> 1 then
        leave main_sql;
    end if;
    set o_locked_now = 1;

    call int_end_user_sessions(i_user_id);

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
  end
$$
delimiter ;
