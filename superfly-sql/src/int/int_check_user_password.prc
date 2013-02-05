drop function if exists int_check_user_password;
delimiter $$
create function int_check_user_password(
        i_user_name         varchar(32),
        i_user_password     text,
        i_ip_address        varchar(15),
        i_session_info      text
) returns int(10) language sql not deterministic
 main_sql:
  begin
    declare v_user_id   int(10);

    set v_user_id   = null;

    select user_id
      into v_user_id
      from users u
     where     u.user_name = i_user_name
           and u.user_password = i_user_password
           and coalesce(u.is_account_locked, 'N') = 'N';

    if v_user_id is null then
      update users u
         set u.logins_failed    = coalesce(u.logins_failed, 0) + 1
       where u.user_name = i_user_name
             and coalesce(u.is_account_locked, 'N') = 'N';

      insert into unauthorised_access
            (
               printed_user_name,
               access_date,
               ip_address,
               session_info
            )
      values (i_user_name, now(), i_ip_address, i_session_info);
    else
      update users u
         set u.last_login_date = now(), u.logins_failed = null
       where u.user_name = i_user_name
             and coalesce(u.is_account_locked, 'N') = 'N';
    end if;

    return v_user_id;
  end
$$
delimiter ;
