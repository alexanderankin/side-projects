create table data_point
(
    id         bigserial primary key,
    x          bigint not null,
    y          bigint not null,
    z          bigint not null,
    data_bytes bytea  not null
);

create index data_point_x on data_point (x);
