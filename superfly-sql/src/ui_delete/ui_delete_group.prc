drop procedure if exists ui_delete_group;
delimiter $$
create procedure ui_delete_group(i_grop_id int(10))
 main_sql:
  begin
    declare v_ssys_id   int(10);

    delete from role_groups
     where grop_grop_id = i_grop_id;

    delete from group_actions
     where grop_grop_id = i_grop_id;

    select ssys_ssys_id
      into v_ssys_id
      from groups
     where grop_id = i_grop_id;

    delete from groups
     where grop_id = i_grop_id;

    call ui_check_expired_sessions(v_ssys_id, null, null, null);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_delete_group',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );