drop procedure if exists ui_get_last_event_id;
delimiter $$
create procedure ui_get_last_event_id(i_subsystem_name varchar(32))
 main_sql:
  begin
    -- only the caller's subsystem: a global max would disclose the volume of foreign events
    select coalesce(max(e.event_id), 0) last_event_id
      from events e
     where e.event_time < now() - interval 5 second -- same stability horizon as ui_get_events
       and e.subsystem_id = (select ssys_id from subsystems where subsystem_name = i_subsystem_name limit 1)
     ;
  end
$$
delimiter ;
call save_routine_information('ui_get_last_event_id',
                              concat_ws(',', 'last_event_id bigint')
     );
