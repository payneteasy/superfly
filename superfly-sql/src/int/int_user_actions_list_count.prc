drop procedure if exists int_user_actions_list_count;
delimiter $$
create procedure int_user_actions_list_count(i_user_id int(10),
                                             i_ssys_list text,
                                             i_mapping_status varchar(1),
                                             i_action_name varchar(100)
)
 main_sql:
  begin
    select count(1) records_count
      from           users u
                   join
                     user_roles ur
                   on ur.user_user_id = u.user_id
                 join
                   role_actions ra
                 on ur.role_role_id = ra.role_role_id
               join
                 actions a
               on a.actn_id = ra.actn_actn_id
                  and a.action_name like
                       concat('%', coalesce(i_action_name, ''), '%')
             join
               subsystems ss
             on a.ssys_ssys_id = ss.ssys_id
                and instr(concat(',', coalesce(i_ssys_list, ss.ssys_id), ','),
                          concat(',', ss.ssys_id, ',')
                   ) > 0
           left join
             user_role_actions ura
           on ura.ract_ract_id = ra.ract_id and ura.user_user_id = u.user_id
     where u.user_id = i_user_id
           and(   (i_mapping_status = "M" and ura.urac_id is not null)
               or(i_mapping_status = "U" and ura.urac_id is null)
               or coalesce(i_mapping_status, "A") = "A");
  end
$$
delimiter ;