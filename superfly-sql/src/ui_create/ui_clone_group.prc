drop procedure if exists ui_clone_group;
delimiter $$
create procedure ui_clone_group(i_new_group_name varchar(32),
                                i_templete_grop_id int(10),
                                out o_grop_id int(10)
)
 main_sql:
  begin
    declare v_grop_id   int(10);

    insert into groups
          (
             group_name, ssys_ssys_id
          )
      select i_new_group_name, ssys_ssys_id
        from groups
       where grop_id = i_templete_grop_id;

    set v_grop_id   = last_insert_id();
    set o_grop_id   = v_grop_id;

    insert into role_groups
          (
             role_role_id, grop_grop_id
          )
      select role_role_id, v_grop_id
        from role_groups
       where grop_grop_id = i_templete_grop_id;

    insert into group_actions
          (
             grop_grop_id, actn_actn_id
          )
      select v_grop_id, actn_actn_id
        from group_actions
       where grop_grop_id = i_templete_grop_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_clone_group',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );
