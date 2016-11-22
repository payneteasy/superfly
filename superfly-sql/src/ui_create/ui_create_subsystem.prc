drop procedure if exists ui_create_subsystem;
delimiter $$
create procedure ui_create_subsystem(i_subsystem_name varchar(32),
                                     i_subsystem_title varchar(255),
                                     i_callback_information varchar(64),
                                     i_send_callbacks varchar(1),
                                     i_allow_list_users varchar(1),
                                     i_ssrv_id int(10),
                                     i_subsystem_url varchar(255),
                                     i_landing_url varchar(255),
                                     i_login_form_css_url varchar(255),
                                     out o_ssys_id int(10)
)
 main_sql:
  begin
    insert into subsystems
          (
             subsystem_name,
             subsystem_title,
             callback_information,
             send_callbacks,
             fixed,
             allow_list_users,
             ssrv_ssrv_id,
             subsystem_url,
             landing_url,
             login_form_css_url
          )
    values (
            i_subsystem_name,
            i_subsystem_title,
            i_callback_information,
            i_send_callbacks,
            'N',
            i_allow_list_users,
            i_ssrv_id,
            i_subsystem_url,
            i_landing_url,
            i_login_form_css_url
           );

    set o_ssys_id   = last_insert_id();

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('ui_create_subsystem',
                              concat_ws(',',
                                        'status varchar',
                                        'error_message varchar'
                              )
     );