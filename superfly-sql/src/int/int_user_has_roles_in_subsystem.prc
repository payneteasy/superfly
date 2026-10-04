drop procedure if exists int_user_has_roles_in_subsystem;
delimiter $$
create procedure int_user_has_roles_in_subsystem(
        i_user_name         varchar(32),
        i_subsystem_name    varchar(32)
)
 main_sql:
  begin
    declare v_count int(10);

    select count(*)
      into v_count
      from         users u
                 join
                   user_roles ur
                 on ur.user_user_id = u.user_id
               join
                 roles r
               on r.role_id = ur.role_role_id
             join
               subsystems ss
             on r.ssys_ssys_id = ss.ssys_id
     where u.user_name = i_user_name and ss.subsystem_name = i_subsystem_name;

    if v_count > 0 then
      select 'Y' has_roles;
    else
      select 'N' has_roles;
    end if;
  end
$$
delimiter ;
call save_routine_information('int_user_has_roles_in_subsystem',
                              concat_ws(',',
                                        'has_roles varchar'
                              )
     );
