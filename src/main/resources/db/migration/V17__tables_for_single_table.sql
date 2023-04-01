create table if not exists users_single_table
(
    id                   bigserial primary key,
    username             varchar(128) unique not null,
    programming_language varchar(128),
    project_name         varchar(128),
    user_type            varchar(128)        not null
);

insert into users_single_table (username, programming_language, project_name, user_type)
values ('defaultProgrammer', 'JAVA', null, 'PROGRAMMER'),
       ('defaultManager', null, 'defaultProject', 'MANAGER');

