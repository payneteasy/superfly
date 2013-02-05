drop procedure if exists touch_sessions;
delimiter $$
create procedure touch_sessions(i_session_ids mediumtext)
 main_sql:
  begin
    update sessions s, sso_sessions ss
      set ss.access_date = now()
      where s.ssos_ssos_id = ss.ssos_id
        and s.actions_expired = 'N'
        and s.session_expired = 'N';
  end
$$
delimiter ;
