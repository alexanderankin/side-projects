--liquibase formatted sql
--changeset author:author
-- https://docs.liquibase.com/concepts/changelogs/sql-format.html

create sequence input_task_seq increment by 50 cache 50;
create table input_task
(
    id          int         not null primary key,
    created_at  timestamptz not null,
    finished_at timestamptz null
);

create sequence input_task_item_seq increment by 50 cache 50;
create table input_task_item
(
    id        int not null primary key,
    parent_id int not null references input_task (id),
    value     int not null
);

create table job_queue
(
    id         serial primary key,
    payload    jsonb       not null,
    created_at timestamptz not null default now()
);

create table job_in_progress
(
    id             int primary key,
    payload        jsonb       not null,
    created_at     timestamptz not null default now(),
    job_created_at timestamptz not null,
    finished_at    timestamptz,
    failure_reason TEXT
);

CREATE INDEX job_in_progress_failed_cursor_idx
    ON job_in_progress (id)
    WHERE failure_reason IS NOT NULL;
