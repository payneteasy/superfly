drop procedure if exists int_end_user_sessions;
delimiter $$
create procedure int_end_user_sessions(i_user_id int(10))
begin
       -- delete_sso_session for every SSO session of the user; subsystem sessions opened without
       -- an SSO session are expired as well, so the scheduler sends a logout for each of them
       update sessions set session_expired = 'Y' where user_user_id = i_user_id;

       delete st
         from subsystem_tokens st, sso_sessions ss
        where st.ssos_ssos_id = ss.ssos_id
          and ss.user_user_id = i_user_id;
       delete from sso_sessions where user_user_id = i_user_id;
end
$$
delimiter ;
