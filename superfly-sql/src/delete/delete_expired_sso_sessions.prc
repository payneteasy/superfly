drop procedure if exists delete_expired_sso_sessions;
delimiter $$
create procedure delete_expired_sso_sessions(i_max_age_minutes int(10))
 main_sql:
  begin
    declare v_boundary_date datetime;

    set v_boundary_date = date_sub(now(), interval i_max_age_minutes minute);

    delete st
      from subsystem_tokens st, sso_sessions ss
      where st.ssos_ssos_id = ss.ssos_id
        and ss.access_date < v_boundary_date;
    delete from sso_sessions where access_date < v_boundary_date;
  end
$$
delimiter ;