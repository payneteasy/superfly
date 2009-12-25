drop procedure if exists ui_change_user_roles;
delimiter $$
create procedure ui_change_user_roles(i_user_id int(10),
                                      i_role_list_link text,
                                      i_role_list_unlink text
)
 main_sql:
  begin
    if i_role_list_link is not null then
      call int_link_user_roles(i_user_id, i_role_list_link);
    end if;

    if i_role_list_unlink is not null then
      call int_unlink_user_roles(i_user_id, i_role_list_unlink);
    end if;

    commit;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_change_user_roles',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );