drop procedure if exists touch_sessions;
delimiter $$
create procedure touch_sessions(i_session_ids mediumtext, i_subsystem_name varchar(32))
 main_sql:
  begin
    -- only the listed sessions of the caller's subsystem are touched; a malformed list touches nothing
    if i_session_ids is null or i_session_ids not regexp '^[0-9]+(,[0-9]+)*$' or i_subsystem_name is null then
      leave main_sql;
    end if;
    update sessions s, sso_sessions ss, subsystems sys
      set ss.access_date = now()
      where s.ssos_ssos_id = ss.ssos_id
        and s.ssys_ssys_id = sys.ssys_id
        and sys.subsystem_name = i_subsystem_name
        and find_in_set(s.sess_id, i_session_ids)
        and s.actions_expired = 'N'
        and s.session_expired = 'N';
  end
$$
delimiter ;
