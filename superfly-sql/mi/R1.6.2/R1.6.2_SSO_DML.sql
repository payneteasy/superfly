update subsystems set send_callbacks = 'Y' where send_callbacks not in ('Y', 'N') or send_callbacks is null;

commit;
