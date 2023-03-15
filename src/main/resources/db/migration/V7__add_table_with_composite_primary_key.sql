create table if not exists users_with_composite_primary_key
(
    firstname  varchar(128) not null,
    lastname   varchar(128) not null,
    birth_date date         not null,
    username   varchar(128) not null unique,
    role       varchar(128),
    info       jsonb,
    primary key (firstname, lastname, birth_date)
);