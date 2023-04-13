create table if not exists audit
(
    id             bigserial not null primary key,
    entity_class   varchar(255),
    entity_content varchar(255),
    entity_id      varchar(128),
    operation      varchar(128)
)