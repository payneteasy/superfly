drop procedure if exists get_valid_sso_session;
delimiter $$
create procedure get_valid_sso_session(i_identifier varchar(64))
 main_sql:
  begin

    select ssos_id sso_session_id, identifier
      from sso_sessions
      where identifier = i_identifier;
  
  end
;
$$
delimiter ;
call save_routine_information('get_valid_sso_session',
                              concat_ws(',',
                                        'sso_session_id int',
                                        'identifier varchar'
                              )
     );