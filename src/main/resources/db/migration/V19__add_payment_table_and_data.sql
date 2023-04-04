create table if not exists payment
(
    id          bigserial primary key,
    amount      int    not null,
    receiver_id bigint not null references users (id)
);


insert into public.company (name)
values ('Microsoft'),
       ('Apple'),
       ('Google');

insert into public.users (username, firstname, lastname, birth_date, role, info, company_id)
values ('BillGates', 'Bill', 'Gates', '1955-10-28', null, null,
        (select id from company where name = 'Microsoft')),
       ('SteveJobs', 'Steve', 'Jobs', '1955-02-24', null, null,
        (select id from company where name = 'Apple')),
       ('SergeyBrin', 'Sergey', 'Brin', '1973-08-21', null, null,
        (select id from company where name = 'Google')),
       ('TimCook', 'Tim', 'Cook', '1960-11-01', null, null,
        (select id from company where name = 'Apple')),
       ('DianeGreene', 'Diane', 'Greene', '1955-01-01', null, null,
        (select id from company where name = 'Google'));

insert into public.payment (id, amount, receiver_id)
values (1, 100, (select id from users where username = 'BillGates')),
       (2, 300, (select id from users where username = 'BillGates')),
       (3, 500, (select id from users where username = 'BillGates')),
       (4, 250, (select id from users where username = 'SteveJobs')),
       (5, 600, (select id from users where username = 'SteveJobs')),
       (6, 500, (select id from users where username = 'SteveJobs')),
       (7, 400, (select id from users where username = 'TimCook')),
       (8, 300, (select id from users where username = 'TimCook')),
       (9, 500, (select id from users where username = 'SergeyBrin')),
       (10, 500, (select id from users where username = 'SergeyBrin')),
       (11, 500, (select id from users where username = 'SergeyBrin')),
       (12, 300, (select id from users where username = 'DianeGreene')),
       (13, 300, (select id from users where username = 'DianeGreene')),
       (14, 300, (select id from users where username = 'DianeGreene'));



