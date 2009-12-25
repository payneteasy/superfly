drop procedure if exists int_user_roles_list_count;
delimiter $$
create procedure int_user_roles_list_count(i_user_id int(10),
                                           i_mapping_status varchar(1)
)
 main_sql:
  begin
    select count(1) records_count
      from       users u
               join
                 roles r
             join
               subsystems ss
             on ss.ssys_id = r.ssys_ssys_id
           left join
             user_roles ur
           on ur.user_user_id = u.user_id and ur.role_role_id = r.role_id
     where u.user_id = i_user_id
           and(   (i_mapping_status = "M" and ur.urol_id is not null)
               or(i_mapping_status = "U" and ur.urol_id is null)
               or coalesce(i_mapping_status, "A") = "A");
  end
$$
delimiter ;