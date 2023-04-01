create table if not exists users_join_inheritance_strategy
(
    id       bigserial primary key,
    username varchar(128) not null
);

create table if not exists programmer_join_inheritance_strategy
(
    user_id              bigserial primary key references users_join_inheritance_strategy (id),
    programming_language varchar(128) not null
);

create table if not exists manager_join_inheritance_strategy
(
    user_id      bigserial primary key references users_join_inheritance_strategy (id),
    project_name varchar(128) unique not null
);

insert into users_join_inheritance_strategy (username)
values ('defaultProgrammer'),
       ('defaultManager');

insert into programmer_join_inheritance_strategy (user_id, programming_language)
values ((select id
         from users_join_inheritance_strategy
         where username = 'defaultProgrammer'),
        'JAVA');

insert into manager_join_inheritance_strategy (user_id, project_name)
values ((select id
         from users_join_inheritance_strategy
         where username = 'defaultManager'),
        'defaultProject');