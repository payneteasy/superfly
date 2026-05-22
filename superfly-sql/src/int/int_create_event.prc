drop procedure if exists int_create_event;
delimiter $$
create procedure int_create_event(
        i_event_code        varchar(32),
        i_event_data        varchar(128)
)
begin
       declare v_event_type_id int;

       select event_type_id into v_event_type_id
         from event_types
        where event_code = i_event_code
        limit 1;

       if v_event_type_id is null then
           signal sqlstate '45000'
               set message_text = concat('Unknown event_code: ', i_event_code);
       end if;

       insert into events (event_time, event_type_id, event_data)
       values (now(), v_event_type_id, i_event_data);
end
$$
delimiter ;
