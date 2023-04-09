alter table if exists payment
    add column if not exists version bigint not null default 0;