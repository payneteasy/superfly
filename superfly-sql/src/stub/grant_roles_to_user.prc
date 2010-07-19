drop procedure if exists grant_roles_to_user;
delimiter $$
create procedure grant_roles_to_user(i_user_id       int(10),
                               i_subsystem_name    varchar(32),
                               i_principal_list    text
                              )
 main_sql:
  begin
    insert into user_roles(user_user_id, role_role_id)
      select i_user_id, r.role_id
        from subsystems ss
             join roles r
               on r.ssys_ssys_id = ss.ssys_id
             left join user_roles ur
               on (r.role_id = ur.role_role_id and ur.user_user_id = i_user_id)
       where     instr(concat(',', i_principal_list, ','), concat(',', r.principal_name, ','))
             and ur.urol_id is null
             and ss.subsystem_name = i_subsystem_name;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('grant_roles_to_user', concat_ws(',', 'status varchar', 'error_message varchar'));