-- subsystem main tokens are stored as 'sha256:' + hex(SHA-256(token)); 7 + 64 characters do not fit varchar(64)
call run_install_command('alter table subsystems modify column subsystem_token varchar(80)', '42S21');

-- clients keep working with their current tokens; an already converted value is not hashed again
update subsystems
   set subsystem_token = concat('sha256:', sha2(subsystem_token, 256))
 where subsystem_token is not null
   and subsystem_token <> ''
   and subsystem_token not like 'sha256:%';


-- legacy (not pbkdf2) hashes in user_history outside the reuse check window are dropped: the window is the 5 newest
-- rows of a user (the one standing for the current password and the 4 before it); the rows inside it are replaced
-- by pbkdf2 hashes at login or leave the window with the next password changes
delete uh
  from user_history uh
       inner join (select a.user_user_id, a.number_history
                     from user_history a
                          inner join user_history b
                                  on b.user_user_id = a.user_user_id and b.number_history > a.number_history
                    group by a.user_user_id, a.number_history
                   having count(*) >= 5) old
               on old.user_user_id = uh.user_user_id and old.number_history = uh.number_history
 where uh.user_password not like 'pbkdf2-sha256$%';
