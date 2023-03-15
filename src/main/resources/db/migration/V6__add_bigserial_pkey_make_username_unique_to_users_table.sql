alter table if exists users
    drop constraint if exists users_pkey;

alter table if exists users
    add column if not exists id bigserial
        constraint users_pkey primary key;

alter table if exists users
    add constraint users_username_key unique (username);

create table if not exists users_with_sequence
(
    id         bigint,
    username   varchar(128) not null
        unique,
    firstname  varchar(128),
    lastname   varchar(128),
    birth_date date,
    role       varchar(128),
    info       jsonb
);

create sequence if not exists users_with_sequence_id_seq
    increment by 1
    minvalue 1
    owned by users_with_sequence.id;

create table if not exists users_with_table_generator
(
    id         bigint,
    username   varchar(128) not null
        unique,
    firstname  varchar(128),
    lastname   varchar(128),
    birth_date date,
    role       varchar(128),
    info       jsonb
);

create table if not exists sequences_table
(
    table_name varchar primary key,
    pk_value   bigint not null
);
