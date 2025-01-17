--liquibase formatted sql
--changeset author:author
-- https://docs.liquibase.com/concepts/changelogs/sql-format.html

create table task
(
    id          uuid         not null primary key,
    title       varchar(150) not null,
    description text
);
