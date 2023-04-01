create sequence if not exists table_per_class
    as           bigint
    increment    1
    minvalue     1
    start with   1;

create table if not exists programmer_table_per_class
(
    id                   bigint primary key default nextval('table_per_class'),
    username             varchar(128),
    programming_language varchar(128)
);

create table if not exists manager_table_per_class
(
    id           bigint primary key default nextval('table_per_class'),
    username     varchar(128),
    project_name varchar(128)
);

insert into programmer_table_per_class (id, username, programming_language)
values ((select nextval from nextval('table_per_class')),
        'defaultProgrammer',
        'JAVA');

insert into manager_table_per_class (id, username, project_name)
values ((select nextval from nextval('table_per_class')),
        'defaultManager',
        'defaultProject');

