drop procedure if exists create_subsystem_token_if_can_login;
delimiter $$
create procedure create_subsystem_token_if_can_login(
        i_sso_sess_id       int(10),
        i_subsystem_name    varchar(32),
        i_unique_token      varchar(64)
)
 main_sql:
  begin
    declare v_sso_sess_id   int(10);
    declare v_user_id       int(10);
    declare v_ssys_id       int(10);

    select ssos_id, user_user_id
      into v_sso_sess_id, v_user_id
      from sso_sessions
      where ssos_id = i_sso_sess_id;

    if v_sso_sess_id is null then
      -- no such SSO session - return
      select null from dual where false;
      leave main_sql;
    end if;

    select ssys_id
      into v_ssys_id
      from subsystems
      where subsystem_name = i_subsystem_name;

    if v_ssys_id is null then
      -- no such subsystem - return
      select null from dual where false;
      leave main_sql;
    end if;

    if int_has_actions(v_user_id, i_subsystem_name) = 'N' then
      -- no actions in that subsystem - return
      select null from dual where false;
      leave main_sql;
    end if;

    -- removing tokens (for that subsystem) that already exist
    delete from subsystem_tokens
      where ssos_ssos_id = v_sso_sess_id
        and ssys_ssys_id = v_ssys_id;

    insert into subsystem_tokens(
        ssos_ssos_id,
        ssys_ssys_id,
        token,
        created_date
    ) values (
        v_sso_sess_id,
        v_ssys_id,
        i_generated_token,
        now()
    );

    select
        i_generated_token subsystem_token,
        landing_url
      from subsystems where ssys_id = v_ssys_id;
  end
$$
delimiter ;
call save_routine_information('create_subsystem_token_if_can_login',
                              concat_ws(',',
                                        'subsystem_token varchar',
                                        'landing_url varchar'
                              )
     );
