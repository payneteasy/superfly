drop procedure if exists encrypt_subsystem_private_key;
delimiter $$
create procedure encrypt_subsystem_private_key(i_ssys_id int(10), i_private_key text)
 main_sql:
  begin
    -- a key that is already encrypted (e.g. regenerated in the UI while the startup task runs) is never overwritten
    update subsystems
       set private_key = i_private_key
     where ssys_id = i_ssys_id
       and private_key not like 'v2:%';
  end
$$
delimiter ;
