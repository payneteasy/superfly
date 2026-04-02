drop procedure if exists int_create_event;
delimiter $$
create procedure int_create_event(
        i_event_code        varchar(32),
        i_event_data        varchar(128)
)
begin
       insert into events (event_time,event_type_id,event_data)
       select now()
              ,event_type_id
              ,i_event_data
         from event_types
       where event_code=i_event_code limit 1;
end
$$
delimiter ;
