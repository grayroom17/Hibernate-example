alter table if exists users
    add column if not exists info jsonb;