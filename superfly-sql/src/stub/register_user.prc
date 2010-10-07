drop procedure if exists register_user;
delimiter $$
create procedure register_user(i_user_name         varchar(32),
                               i_user_password     varchar(32),
                               i_user_email        varchar(255),
                               i_subsystem_name    varchar(32),
                               i_principal_list    text,
                               i_name 		   varchar(32),   
                               i_surname	   varchar(32),
                               i_secret_question   varchar(255),
                               i_secret_answer     varchar(255),
                               i_salt              varchar(128), 			 
                               out o_user_id       int(10)
                              )
 main_sql:
  begin
	select user_id from users where user_name = i_user_name into o_user_id;
	
	if o_user_id is not null then
		select 'duplicate' status, 'User already exists' error_message;
		leave main_sql;
	end if;
  
    insert into users(user_name, user_password, email, is_account_locked,`name`,surname,secret_question,secret_answer,salt)
         values (i_user_name, i_user_password, i_user_email,'N',i_name,i_surname,i_secret_question,i_secret_answer,i_salt);

    set o_user_id   = last_insert_id();
    
    if i_principal_list is not null then
	    insert into user_roles(user_user_id, role_role_id)
	      select o_user_id, r.role_id
	        from subsystems ss
	             join roles r
	               on r.ssys_ssys_id = ss.ssys_id
	             left join user_roles ur
	               on (r.role_id = ur.role_role_id and ur.user_user_id = o_user_id)
	       where     instr(concat(',', i_principal_list, ','), concat(',', r.principal_name, ','))
	             and ur.urol_id is null
	             and ss.subsystem_name = i_subsystem_name;
	end if;

    select 'OK' status, null error_message;
  end
$$
delimiter ;
call save_routine_information('register_user', concat_ws(',', 'status varchar', 'error_message varchar'));