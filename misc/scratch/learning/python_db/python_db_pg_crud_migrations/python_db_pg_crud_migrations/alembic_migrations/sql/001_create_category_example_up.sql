create extension if not exists "uuid-ossp";

create table category
(
    id         bigserial primary key,
    name       varchar(150) not null unique,
    created_at timestamp without time zone not null default now(),
    updated_at timestamp without time zone not null default now()
);

create table example
(
    id          uuid primary key default uuid_generate_v4(),
    category_id bigint       not null references category(id) on delete cascade,
    name        varchar(150) not null,
    created_at  timestamp without time zone not null default now(),
    updated_at  timestamp without time zone not null default now(),

    constraint example_uq_category_name
        unique (category_id, name)
);
