create table if not exists company_locale
(
    company_id  bigint  not null references company (id),
    language    char(2) not null,
    description varchar(128),
    primary key (company_id, language)
)