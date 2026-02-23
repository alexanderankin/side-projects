--liquibase formatted sql
--changeset author:author splitStatements:false stripComments:false endDelimiter:;
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
    id             serial      not null primary key,
    created_at     timestamptz not null default now(),
    payload        jsonb       not null,
    attempt        int         not null default 1,
    original_id    int,
    started_at     timestamptz,
    finished_at    timestamptz,
    failure_reason text,
    retried_at     timestamptz
);

create index job_queue_queue_latest_desc_idx
    on job_queue (created_at desc)
    where started_at is null;

create index job_queue_retry_latest_desc_idx
    on job_queue (created_at desc)
    where failure_reason is not null
        and retried_at is null;

create index job_queue_list_failed_idx
    on job_queue (id)
    where failure_reason is not null;

create function queue_latest_job()
    returns setof job_queue
as
$$
begin
    return query
    update job_queue
    set started_at = now()
    where id = (select id from job_queue where started_at is null order by created_at desc for update skip locked limit 1)
    returning job_queue.*;
end;
$$ language plpgsql;

create function retry_failed_job(job_queue_item_id int)
    returns setof job_queue
as
$$
begin
    return query
    with q_item as (
        update job_queue
            set retried_at = now()
            where id = job_queue_item_id
            returning *)
    insert
    into job_queue(payload, attempt, original_id)
    values (q_item.payload, q_item.attempt + 1, q_item.id)
    returning *;
end;
$$ language plpgsql;

create function retry_latest_job()
    returns setof job_queue
as
$$
begin
    return query
    with q_item as (
        update job_queue
            set retried_at = now()
            where created_at = (select max(created_at)
                                from job_queue
                                where failure_reason is not null
                                  and retried_at is null)
            returning *)
    insert
    into job_queue(payload, attempt, original_id)
    values (q_item.payload, q_item.attempt + 1, q_item.id)
    returning *;
end;
$$ language plpgsql;

create function retry_all_failed_jobs()
    returns setof job_queue
as
$$
begin
    return query
    with queue_item as (
        update job_queue
            set retried_at = now()
            where failure_reason is not null and retried_at is null
            returning *)
    insert
    into job_queue(payload, attempt, original_id)
    values (queue_item.payload, queue_item.attempt + 1, queue_item.id)
    returning *;
end;
$$ language plpgsql;
