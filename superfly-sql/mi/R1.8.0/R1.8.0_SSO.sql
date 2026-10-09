-- subsystem main tokens are stored as 'sha256:' + hex(SHA-256(token)); 7 + 64 characters do not fit varchar(64)
call run_install_command('alter table subsystems modify column subsystem_token varchar(80)', '42S21');

-- clients keep working with their current tokens; an already converted value is not hashed again
update subsystems
   set subsystem_token = concat('sha256:', sha2(subsystem_token, 256))
 where subsystem_token is not null
   and subsystem_token <> ''
   and subsystem_token not like 'sha256:%';
