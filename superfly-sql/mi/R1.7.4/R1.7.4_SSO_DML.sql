delete from event_types where event_type_id=1;
insert into event_types (event_type_id, event_code, event_name)
    values (1,'PASSWORD_RESET','Password reset');
delete from event_types where event_type_id=2;
insert into event_types (event_type_id, event_code, event_name)
    values (2,'ACCOUNT_LOCK','Account lock');
delete from event_types where event_type_id=3;
insert into event_types (event_type_id, event_code, event_name)
    values (3,'ACCOUNT_SUSPEND','Account suspend');
commit;
