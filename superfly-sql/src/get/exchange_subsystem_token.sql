drop procedure if exists exchange_subsystem_token;
delimiter $$
create procedure exchange_subsystem_token(
        i_subsystem_token   varchar(64),
        i_subsystem_name    varchar(32)
)
 main_sql:
  begin
    declare v_user_id           int(10);
    declare v_subsystem_name    varchar(32);
    declare v_sso_sess_id       int(10);
    declare v_ssys_id           int(10);
    declare v_is_locked         varchar(1);
    declare v_is_suspended      varchar(1);
    declare c_token_ttl_seconds int(10) default 30; -- must cover the browser redirect; keep <= delete_expired_tokens age

    -- the token is valid only for the calling subsystem, within its TTL and for a non-locked user;
    -- every other case looks the same as an unknown token (and does not burn the token)
    select u.user_id, s.subsystem_name, st.ssos_ssos_id, s.ssys_id
      into v_user_id, v_subsystem_name, v_sso_sess_id, v_ssys_id
      from subsystem_tokens st
        join sso_sessions ss on st.ssos_ssos_id = ss.ssos_id
        join users u on ss.user_user_id = u.user_id
        join subsystems s on st.ssys_ssys_id = s.ssys_id
     where     st.token = i_subsystem_token
           and s.subsystem_name = i_subsystem_name
           and st.created_date > date_sub(now(), interval c_token_ttl_seconds second)
           and coalesce(u.is_account_locked, 'N') = 'N'
           and u.is_account_suspended = 'N';

    if v_user_id is null then
      commit; -- to save unauthorized_access INSERT

      select null
        from dual
       where false;

      leave main_sql;
    end if;

    -- the user row is locked before the token and the SSO session: ui_lock_user and ui_suspend_user lock the rows
    -- in the order users, sessions, subsystem_tokens, sso_sessions, and the same order here keeps the exchange from
    -- deadlocking with them. The flags are read again under the lock: a user locked meanwhile is refused
    -- like an unknown token, and the token is kept
    select coalesce(is_account_locked, 'N'), is_account_suspended
      into v_is_locked, v_is_suspended
      from users
     where user_id = v_user_id
       lock in share mode;

    if v_is_locked is null or v_is_locked <> 'N' or v_is_suspended is null or v_is_suspended <> 'N' then
      select null
        from dual
       where false;

      leave main_sql;
    end if;

    -- destroying token first: only the exchange that actually deleted the row may proceed,
    -- so concurrent exchanges of the same token yield exactly one session
    delete from subsystem_tokens where token = i_subsystem_token and ssys_ssys_id = v_ssys_id;

    if row_count() <> 1 then
      commit;

      select null
        from dual
       where false;

      leave main_sql;
    end if;

    -- touching SSO session
    update sso_sessions set access_date = now() where ssos_id = v_sso_sess_id;

    call int_get_user_actions(
        v_user_id,
        v_subsystem_name,
        null,
        null,
        'Y',
        v_sso_sess_id
    );
  
  end
;
$$
delimiter ;
call save_routine_information('exchange_subsystem_token',
                              concat_ws(',',
                                        'username varchar',
                                        'is_otp_optional varchar',
                                        'otp_code varchar',
                                        'otp_type_id int',
                                        'session_id int',
                                        'role_role_name varchar',
                                        'role_principal_name varchar',
                                        'role_callback_information varchar',
                                        'role_action_action_name varchar',
                                        'role_action_log_action varchar'
                              )
     );