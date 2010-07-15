drop procedure if exists register_user;
delimiter $$
create procedure register_user(i_user_name         varchar(32),
                               i_user_password     varchar(32),
                               i_user_email        varchar(255),
                               i_subsystem_name    varchar(32),
                               i_principal_list    text,
                               out o_user_id       int(10)
                              )
 main_sql:
  begin
    insert into users(user_name, user_password, email)
         values (i_user_name, i_user_password, i_user_email);

    set o_user_id   = last_insert_id();

    insert into user_roles(user_user_id, role_role_id)
      select o_user_id, r.role_id
        from subsystems ss
             join roles r
               on r.ssys_ssys_id = ss.ssys_id
             left join user_roles ur
               on r.role_id = ur.role_role_id
       where     instr(concat(',', i_principal_list, ','), concat(',', r.role_name, ','))
             and ur.urol_id is null
             and ss.subsystem_name = i_subsystem_name;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('register_user', concat_ws(',', 'status varchar', 'error_message varchar'));