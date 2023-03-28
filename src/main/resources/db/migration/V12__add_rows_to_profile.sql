insert into users (username, company_id)
values ('userWithProfile 1', 1),
       ('userWithProfile 2', 1),
       ('userWithProfile 3', 1)
on conflict do nothing;

insert into profile (user_id, language, programming_language)
values (7, 'RU', 'Java'),
       (8, 'EN', 'PHP'),
       (9, 'ES', 'Go')
on conflict do nothing;