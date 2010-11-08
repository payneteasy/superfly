drop procedure if exists update_user_for_description;
delimiter $$
create procedure update_user_for_description(i_user_id		int(11),
											 i_username		varchar(32),
											 i_email		varchar(255),
											 i_name			varchar(255),
											 i_surname		varchar(255),
											 i_public_key 	text
)
 main_sql:
  begin
    update users set
    	email = i_email,
    	name = i_name,
    	surname = i_surname,
    	public_key = i_public_key
    where user_id = i_user_id;
  end                                                    
$$
delimiter ;
