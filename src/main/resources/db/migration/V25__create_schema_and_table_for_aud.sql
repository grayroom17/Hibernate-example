create schema if not exists aud;

create table if not exists aud.revinfo
(
    rev           bigserial not null primary key,
    rev_timestamp bigint,
    user_name     varchar(128)
);

create table if not exists aud.users_aud
(
    id         bigint  not null,
    rev        integer not null references aud.revinfo (rev),
    revtype    smallint,
    birth_date date,
    firstname  varchar(255),
    lastname   varchar(255),
    role       varchar(255),
    username   varchar(255),
    company_id bigint,
    primary key (rev, id)
);

create table if not exists aud.company_aud
(
    id      bigint  not null,
    rev     integer not null references aud.revinfo (rev),
    revtype smallint,
    name    varchar(255),
    primary key (rev, id)
);

create table if not exists aud.payment_aud
(
    id          bigint  not null,
    rev         integer not null references aud.revinfo (rev),
    revtype     smallint,
    amount      integer,
    receiver_id bigint,
    primary key (rev, id)
);

create table if not exists aud.team_aud
(
    id      bigint  not null,
    rev     integer not null references aud.revinfo (rev),
    revtype smallint,
    name    varchar(255),
    primary key (rev, id)
);

create table if not exists aud.users_team_aud
(
    id         bigint  not null,
    rev        integer not null references aud.revinfo (rev),
    revtype    smallint,
    created_by varchar(255),
    joined     timestamp(6) with time zone,
    team_id    bigint,
    user_id    bigint,
    primary key (rev, id)
);

create table aud.profile_aud
(
    id                   bigint  not null,
    rev                  integer not null references aud.revinfo (rev),
    revtype              smallint,
    language             char(2),
    programming_language varchar(255),
    user_id              bigint,
    primary key (rev, id)
);