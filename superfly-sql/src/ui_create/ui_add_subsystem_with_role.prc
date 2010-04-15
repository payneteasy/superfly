drop procedure if exists ui_add_subsystem_with_role;
delimiter $$
create procedure ui_add_subsystem_with_role(i_user_id int(10),
                                i_role_id int(10)
)
 main_sql:
  begin
    insert into user_roles
          (
            user_user_id, role_role_id
          )
    values (i_user_id, i_role_id);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_add_subsystem_with_role',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );
