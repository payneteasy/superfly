drop procedure if exists ui_create_group;
delimiter $$
create procedure ui_create_group(i_group_name varchar(32), i_ssys_id int(10))
 main_sql:
  begin
    insert into groups
          (
             group_name, ssys_ssys_id
          )
    values (i_group_name, i_ssys_id);

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_create_group',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );