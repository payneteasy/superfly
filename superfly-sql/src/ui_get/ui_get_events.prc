drop procedure if exists ui_get_events;
delimiter $$
create procedure ui_get_events(i_last_event_time datetime)
 main_sql:
  begin
    select e.event_id
           ,e.event_time
           ,e.event_data
           ,et.event_code as event_type_code
      from events e
          inner join event_types et on et.event_type_id=e.event_type_id
     where event_time > i_last_event_time;
  end
$$
delimiter ;
call save_routine_information('ui_get_events',
                              concat_ws(',',
                                        'event_id int',
                                        'event_time datetime',
                                        'event_data varchar',
                                        'event_type_code varchar'
                              )
     );
