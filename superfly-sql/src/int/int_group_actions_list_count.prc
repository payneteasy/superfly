drop procedure if exists int_group_actions_list_count;
delimiter $$
create procedure int_group_actions_list_count(i_grop_id int(10),
                                              i_mapping_status varchar(1)
)
 main_sql:
  begin
    select count(1) records_count
      from       groups g
               join
                 subsystems ss
               on g.ssys_ssys_id = ss.ssys_id
             join
               actions a
             on a.ssys_ssys_id = ss.ssys_id
           left join
             group_actions ga
           on ga.grop_grop_id = g.grop_id and ga.actn_actn_id = a.actn_id
     where grop_id = i_grop_id
           and(   (i_mapping_status = "M" and ga.gpac_id is not null)
               or(i_mapping_status = "U" and ga.gpac_id is null)
               or coalesce(i_mapping_status, "A") = "A");
  end
$$
delimiter ;