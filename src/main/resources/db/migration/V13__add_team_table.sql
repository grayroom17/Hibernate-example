create table if not exists team
(
    id   bigserial primary key,
    name varchar(128) not null unique
);

insert into team (name)
values ('Default Team');

create table if not exists users_team
(
    id         bigserial primary key,
    user_id    bigint references users (id),
    team_id    bigint references team (id),
    joined     timestamp    not null,
    created_by varchar(128) not null
);

insert into users_team (user_id, team_id, joined, created_by)
values (1, 1, now(), 'SYSTEM');

create table users_many_to_many_with_composite_primary_key
(
    id       bigserial primary key,
    username varchar(128) not null unique
);

create table if not exists team_many_to_many_with_composite_primary_key
(
    id   bigserial primary key,
    name varchar(128) not null unique
);

create table if not exists users_team_with_composite_primary_key
(
    user_id bigint references users_many_to_many_with_composite_primary_key (id),
    team_id bigint references team_many_to_many_with_composite_primary_key (id),
    primary key (user_id, team_id)
);

insert into users_many_to_many_with_composite_primary_key (username)
values ('User with teams');

insert into team_many_to_many_with_composite_primary_key (name)
values ('freaks'),
       ('managers'),
       ('programmers');

insert into users_team_with_composite_primary_key (user_id, team_id)
values ((select id from users_many_to_many_with_composite_primary_key where username = 'User with teams'),
        (select id from team_many_to_many_with_composite_primary_key where name = 'freaks')),
       ((select id from users_many_to_many_with_composite_primary_key where username = 'User with teams'),
        (select id from team_many_to_many_with_composite_primary_key where name = 'managers')),
       ((select id from users_many_to_many_with_composite_primary_key where username = 'User with teams'),
        (select id from team_many_to_many_with_composite_primary_key where name = 'programmers'));