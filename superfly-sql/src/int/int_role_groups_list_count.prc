drop procedure if exists int_role_groups_list_count;
delimiter $$
create procedure int_role_groups_list_count(i_role_id int(10),
                                            i_mapping_status varchar(1)
)
 main_sql:
  begin
    select count(1) records_count
      from       roles r
               join
                 subsystems ss
               on r.ssys_ssys_id = ss.ssys_id
             join
               groups g
             on g.ssys_ssys_id = ss.ssys_id
           left join
             role_groups rg
           on rg.role_role_id = r.role_id and rg.grop_grop_id = g.grop_id
     where role_id = i_role_id
           and(   (i_mapping_status = "M" and rg.rlgp_id is not null)
               or(i_mapping_status = "U" and rg.rlgp_id is null)
               or coalesce(i_mapping_status, "A") = "A");
  end
$$
delimiter ;