drop procedure if exists ui_create_role;
delimiter $$
create procedure ui_create_role(i_role_name varchar(32),
                                i_principal_name varchar(32),
                                i_ssys_id int(10)
)
 main_sql:
  begin
    insert into role
          (
             role_name, principal_name, ssys_ssys_id
          )
    values (i_role_name, i_principal_name, i_ssys_id);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_create_role',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );