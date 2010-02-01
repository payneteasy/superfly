drop procedure if exists ui_delete_session;
delimiter $$
create procedure ui_delete_session(i_sess_id int(10))
 main_sql:
  begin
    delete from sessions
     where sess_id = i_sess_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_session',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );