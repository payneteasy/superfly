drop procedure if exists ui_get_role_by_name;
delimiter $$
create procedure ui_get_role_by_name(i_role_name varchar(32))
 main_sql:
  begin
    select ss.role_id,
           ss.role_name,
           ss.principal_name,
           ss.ssys_ssys_id as ssys_id
      from roles ss
     where ss.role_name = i_role_name;
  end
$$
delimiter ;
call save_routine_information('ui_get_role_by_name',
                              concat_ws(',',
                                        'role_id int',
                                        'role_name varchar',
                                        'principal_name varchar',
                                        'ssys_id int'
                                       
                              )
     );