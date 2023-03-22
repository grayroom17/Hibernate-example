create table if not exists profile
(
    id                   bigserial primary key,
    user_id              bigint not null unique references users (id),
    language             char(2),
    programming_language varchar(128)
);

create table if not exists profile_primary_key_as_foreign_key
(
    profile_id           bigint primary key references users (id),
    language             char(2),
    programming_language varchar(128)
);