alter table if exists team
    add column if not exists user_count int not null default 0;