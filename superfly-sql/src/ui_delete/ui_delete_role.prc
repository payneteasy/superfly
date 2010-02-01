drop procedure if exists ui_delete_role;
delimiter $$
create procedure ui_delete_role(i_role_id int(10))
 main_sql:
  begin
    declare v_ssys_id   int(10);

    delete from role_groups
     where role_role_id = i_role_id;

    delete from user_roles
     where role_role_id = i_role_id;

    delete ura
      from   role_actions ra
           join
             user_role_actions ura
           on ura.ract_ract_id = ra.ract_id
     where ra.role_role_id = i_role_id;

    delete from role_actions
     where role_role_id = i_role_id;

    select ssys_ssys_id
      into v_ssys_id
      from roles
     where role_id = i_role_id;

    delete from roles
     where role_id = i_role_id;

    call ui_check_expired_sessions(v_ssys_id, null, null, null);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_role',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );