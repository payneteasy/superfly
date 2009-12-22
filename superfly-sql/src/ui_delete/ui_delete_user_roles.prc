drop procedure if exists ui_delete_user_roles;
delimiter $$
create procedure ui_delete_user_roles(i_user_id int(10), i_roles_list text)
 main_sql:
  begin
    delete from user_roles
     where user_user_id = i_user_id
           and instr(concat(',', i_roles_list, ','),
                     concat(',', role_role_id, ',')
              ) > 0;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_user_roles',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );