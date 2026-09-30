insert into event_types (event_type_id, event_code, event_name)
    values (1,'PASSWORD_RESET','Password reset'),
           (2,'ACCOUNT_LOCK','Account lock'),
           (3,'ACCOUNT_SUSPEND','Account suspend')
    on duplicate key update event_code=values(event_code), event_name=values(event_name);
commit;
