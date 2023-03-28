create table if not exists company
(
    id   bigserial primary key,
    name varchar(18) not null unique
);

alter table if exists users
    add column if not exists
        company_id bigint;

alter table if exists users
    add constraint users_company_id_fkey
        foreign key (company_id)
            references company (id);