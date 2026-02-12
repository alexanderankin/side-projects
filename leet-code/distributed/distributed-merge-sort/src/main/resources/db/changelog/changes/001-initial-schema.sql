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
