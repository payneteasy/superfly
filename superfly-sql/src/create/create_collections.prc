drop procedure if exists create_collections;
delimiter $$
create procedure create_collections()
  begin
    set session group_concat_max_len   = 64 * 1028;
    set @@max_heap_table_size   = 100 * 1024 * 1024;
    create temporary table if not exists temp_actions(
      action_name        varchar(128),
      action_description varchar(512)
      )
      engine = memory;

    select 1;
  end
$$
delimiter ;
