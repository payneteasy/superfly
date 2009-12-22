drop procedure if exists ui_link_role_groups;
delimiter $$
create procedure ui_link_role_groups(i_role_id int(10), i_groups_list text)
 main_sql:
  begin
    insert into role_groups
          (
             role_role_id, grop_grop_id
          )
      select i_role_id, grop_id
        from     roles r
               join
                 groups g
               on g.ssys_ssys_id = r.ssys_ssys_id
                  and instr(concat(',', i_groups_list, ','),
                            concat(',', grop_id, ',')
                     ) > 0
             left join
               role_groups rg
             on rg.grop_grop_id = g.grop_id and rg.role_role_id = r.role_id
       where rg.rlgp_id is null and r.role_id = i_role_id;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_link_role_groups',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );