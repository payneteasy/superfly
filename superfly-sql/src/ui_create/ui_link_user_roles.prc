drop procedure if exists ui_link_user_roles;
delimiter $$
create procedure ui_link_user_roles(i_user_id int(10), i_roles_list text)
 main_sql:
  begin
    insert into user_roles
          (
             user_user_id, role_role_id
          )
      select i_user_id, role_id
        from     users u
               join
                 roles r
               on instr(concat(',', i_roles_list, ','),
                        concat(',', role_id, ',')
                  ) > 0
             left join
               user_roles ur
             on r.role_id = ur.role_role_id and ur.user_user_id = u.user_id
       where ur.urol_id is null and u.user_id = i_user_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_link_user_roles',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );