drop function if exists int_check_user_password;
delimiter $$
create function int_check_user_password(
        i_user_name         varchar(32),
        i_user_password     text,
        i_legacy_password   text,
        i_ip_address        varchar(64),
        i_session_info      text
) returns int(10) language sql not deterministic
 main_sql:
  begin
    declare v_user_id       int(10);
    declare v_matched_new   int(1);

    set v_user_id       = null;
    set v_matched_new   = null;

    -- i_legacy_password is the old SHA-256 hash: a match on it means the stored hash
    -- is rehashed to i_user_password below (only on success)
    select user_id, (u.user_password = i_user_password)
      into v_user_id, v_matched_new
      from users u
     where     u.user_name = i_user_name
           and (   u.user_password = i_user_password
                or (i_legacy_password is not null and u.user_password = i_legacy_password))
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
         set u.last_login_date = now(), u.logins_failed = null, u.completed = 'Y',
             u.user_password = case when v_matched_new = 1 then u.user_password else i_user_password end
       where u.user_name = i_user_name
             and coalesce(u.is_account_locked, 'N') = 'N';
    end if;

    return v_user_id;
  end
$$
delimiter ;
