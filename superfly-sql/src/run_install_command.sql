drop procedure if exists run_install_command;
delimiter $$
create procedure run_install_command(i_ddl_statement      varchar (4000),
                                     i_ignore_error_code  char (5)
                                     )
begin
  declare v_throw_exception tinyint;
  declare v_ddl_statement   varchar (4000);
  declare v_error_code      char (5);
  declare continue handler for sqlstate '42S02' set v_error_code = '42S02';
  declare continue handler for sqlstate 'HY000' set v_error_code = 'HY000';
	-- Create index 42000
	declare continue handler for sqlstate '42000' set v_error_code = '42000';
	-- Add column 42S21
	declare continue handler for sqlstate '42S21' set v_error_code = '42S21';
	-- Change column 42S22
	declare continue handler for sqlstate '42S22' set v_error_code = '42S22';
	-- Rename table
	declare continue handler for sqlstate '42S01' set v_error_code = '42S01';
  set @v_ddl_statement = i_ddl_statement;
  prepare v_stmt from @v_ddl_statement;
  execute v_stmt;
  if v_error_code <> i_ignore_error_code then
    -- The string will appear on the client.
    -- For 6.0 use 
    -- SIGNAL concat('Warning! Exception ', v_error_code, ' is raised')
    deallocate prepare v_stmt;
    set v_throw_exception = concat('Warning! Exception ', v_error_code,
    ' is raised');
  end if;
  deallocate prepare v_stmt;
end$$
delimiter ;