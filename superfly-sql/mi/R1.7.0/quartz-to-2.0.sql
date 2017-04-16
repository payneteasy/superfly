-- 
-- drop tables that are no longer used
-- 
drop table QRTZ_JOB_LISTENERS;
drop table QRTZ_TRIGGER_LISTENERS;
-- 
-- drop columns that are no longer used
-- 
alter table QRTZ_JOB_DETAILS drop column IS_VOLATILE;
alter table QRTZ_TRIGGERS drop column IS_VOLATILE;
alter table QRTZ_FIRED_TRIGGERS drop column IS_VOLATILE;
-- 
-- add new columns that replace the 'IS_STATEFUL' column
-- 
alter table QRTZ_JOB_DETAILS add column IS_NONCONCURRENT bool;
alter table QRTZ_JOB_DETAILS add column IS_UPDATE_DATA bool;
update QRTZ_JOB_DETAILS set IS_NONCONCURRENT = IS_STATEFUL;
update QRTZ_JOB_DETAILS set IS_UPDATE_DATA = IS_STATEFUL;
alter table QRTZ_JOB_DETAILS drop column IS_STATEFUL;
alter table QRTZ_FIRED_TRIGGERS add column IS_NONCONCURRENT bool;
alter table QRTZ_FIRED_TRIGGERS add column IS_UPDATE_DATA bool;
update QRTZ_FIRED_TRIGGERS set IS_NONCONCURRENT = IS_STATEFUL;
update QRTZ_FIRED_TRIGGERS set IS_UPDATE_DATA = IS_STATEFUL;
alter table QRTZ_FIRED_TRIGGERS drop column IS_STATEFUL;
-- 
-- add new 'SCHED_NAME' column to all tables
-- 
alter table QRTZ_BLOB_TRIGGERS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_CALENDARS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_CRON_TRIGGERS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_FIRED_TRIGGERS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_JOB_DETAILS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_LOCKS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_PAUSED_TRIGGER_GRPS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_SCHEDULER_STATE add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_SIMPLE_TRIGGERS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
alter table QRTZ_TRIGGERS add column SCHED_NAME varchar(120) not null DEFAULT 'TestScheduler';
-- 
-- drop all primary and foreign key constraints, so that we can define new ones
-- 
alter table QRTZ_TRIGGERS drop foreign key QRTZ_TRIGGERS_ibfk_1;
alter table QRTZ_BLOB_TRIGGERS drop primary key;
alter table QRTZ_BLOB_TRIGGERS drop foreign key QRTZ_BLOB_TRIGGERS_ibfk_1;
alter table QRTZ_SIMPLE_TRIGGERS drop primary key;
alter table QRTZ_SIMPLE_TRIGGERS drop foreign key QRTZ_SIMPLE_TRIGGERS_ibfk_1;
alter table QRTZ_CRON_TRIGGERS drop primary key;
alter table QRTZ_CRON_TRIGGERS drop foreign key QRTZ_CRON_TRIGGERS_ibfk_1;
alter table QRTZ_JOB_DETAILS drop primary key;
alter table QRTZ_JOB_DETAILS add primary key (SCHED_NAME, JOB_NAME, JOB_GROUP);
alter table QRTZ_TRIGGERS drop primary key;
-- 
-- add all primary and foreign key constraints, based on new columns
-- 
alter table QRTZ_TRIGGERS add primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
alter table QRTZ_TRIGGERS add foreign key (SCHED_NAME, JOB_NAME, JOB_GROUP) references QRTZ_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP);
alter table QRTZ_BLOB_TRIGGERS add primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
alter table QRTZ_BLOB_TRIGGERS add foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
alter table QRTZ_CRON_TRIGGERS add primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
alter table QRTZ_CRON_TRIGGERS add foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
alter table QRTZ_SIMPLE_TRIGGERS add primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
alter table QRTZ_SIMPLE_TRIGGERS add foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
alter table QRTZ_FIRED_TRIGGERS drop primary key;
alter table QRTZ_FIRED_TRIGGERS add primary key (SCHED_NAME, ENTRY_ID);
alter table QRTZ_CALENDARS drop primary key;
alter table QRTZ_CALENDARS add primary key (SCHED_NAME, calendar_name);
alter table QRTZ_LOCKS drop primary key;
alter table QRTZ_LOCKS add primary key (SCHED_NAME, lock_name);
alter table QRTZ_PAUSED_TRIGGER_GRPS drop primary key;
alter table QRTZ_PAUSED_TRIGGER_GRPS add primary key (SCHED_NAME, TRIGGER_GROUP);
alter table QRTZ_SCHEDULER_STATE drop primary key;
alter table QRTZ_SCHEDULER_STATE add primary key (SCHED_NAME, instance_name);
-- 
-- add new simprop_triggers table
-- 
CREATE TABLE QRTZ_SIMPROP_TRIGGERS
 (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 BOOL NULL,
    BOOL_PROP_2 BOOL NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- 
-- create indexes for faster queries
-- 
create index idx_qrtz_j_req_recovery on QRTZ_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_j_grp on QRTZ_JOB_DETAILS(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_j on QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_t_jg on QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_c on QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
create index idx_qrtz_t_g on QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
create index idx_qrtz_t_state on QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
create index idx_qrtz_t_n_state on QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_n_g_state on QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_next_fire_time on QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st on QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_misfire on QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st_misfire on QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_nft_st_misfire_grp on QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_ft_trig_inst_name on QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
create index idx_qrtz_ft_inst_job_req_rcvry on QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_ft_j_g on QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_ft_jg on QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_ft_t_g on QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_tg on QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);


commit;