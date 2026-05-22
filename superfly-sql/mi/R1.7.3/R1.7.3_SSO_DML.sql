alter table subsystems add column if not exists private_key text;
alter table subsystems add column if not exists public_key text;
alter table subsystems add column if not exists encryption_algorithm varchar(16);

commit;
