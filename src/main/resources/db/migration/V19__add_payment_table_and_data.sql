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

insert into public.payment (amount, receiver_id)
values (100, (select id from users where username = 'BillGates')),
       (300, (select id from users where username = 'BillGates')),
       (500, (select id from users where username = 'BillGates')),
       (250, (select id from users where username = 'SteveJobs')),
       (600, (select id from users where username = 'SteveJobs')),
       (500, (select id from users where username = 'SteveJobs')),
       (400, (select id from users where username = 'TimCook')),
       (300, (select id from users where username = 'TimCook')),
       (500, (select id from users where username = 'SergeyBrin')),
       (500, (select id from users where username = 'SergeyBrin')),
       (500, (select id from users where username = 'SergeyBrin')),
       (300, (select id from users where username = 'DianeGreene')),
       (300, (select id from users where username = 'DianeGreene')),
       (300, (select id from users where username = 'DianeGreene'));



