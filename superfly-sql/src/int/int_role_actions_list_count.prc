drop procedure if exists int_role_actions_list_count;
delimiter $$
create procedure int_role_actions_list_count(i_role_id int(10),
                                             i_mapping_status varchar(1),
                                             i_action_name varchar(100)
)
 main_sql:
  begin
    select count(1) records_count
      from       roles r
               join
                 subsystems ss
               on r.ssys_ssys_id = ss.ssys_id
             join
               actions a
             on a.ssys_ssys_id = ss.ssys_id
                and a.action_name like
                     concat('%', coalesce(i_action_name, ''), '%')
           left join
             role_actions ra
           on ra.role_role_id = r.role_id and ra.actn_actn_id = a.actn_id
     where role_id = i_role_id
           and(   (i_mapping_status = "M" and ra.ract_id is not null)
               or(i_mapping_status = "U" and ra.ract_id is null)
               or coalesce(i_mapping_status, "A") = "A");
  end
$$
delimiter ;