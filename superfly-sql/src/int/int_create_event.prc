drop procedure if exists int_create_event;
delimiter $$
create procedure int_create_event(
        i_event_code        varchar(32),
        i_event_data        varchar(128),
        i_subsystem_id      int
)
begin
       declare v_event_type_id int;
       declare v_message       varchar(128);

       select event_type_id into v_event_type_id
         from event_types
        where event_code = i_event_code
        limit 1;

       if v_event_type_id is null then
           set v_message = concat('Unknown event_code: ', i_event_code);
           signal sqlstate '45000'
               set message_text = v_message;
       end if;

       insert into events (event_time, event_type_id, event_data, subsystem_id)
       values (now(), v_event_type_id, i_event_data, i_subsystem_id);
end
$$
delimiter ;
