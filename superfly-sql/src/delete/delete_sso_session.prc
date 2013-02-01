drop procedure if exists delete_sso_session;
delimiter $$
create procedure delete_sso_session(i_identifier varchar(64))
 main_sql:
  begin
    declare v_ssos_id   int(10);

    select ssos_id
      into v_ssos_id
      from sso_sessions
      where identifier = i_identifier;

    if v_ssos_id is not null then
      -- expiring sessions issued by this SSO session
      update sessions set session_expired = 'Y' where ssos_ssos_id = v_ssos_id;

      delete from subsystem_tokens where ssos_ssos_id = v_ssos_id;
      delete from sso_sessions where ssos_id = v_ssos_id;
    end if;
  end
$$
delimiter ;