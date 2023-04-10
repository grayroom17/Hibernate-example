alter table if exists users
    add column if not exists created_at timestamp with time zone not null default now();

alter table if exists users
    add column if not exists updated_at timestamp with time zone;