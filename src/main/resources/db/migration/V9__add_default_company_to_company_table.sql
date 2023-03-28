insert into company (name)
values ('defaultCompany')
on conflict do nothing;

insert into users (username, company_id)
values ('userWithCompany 1', 1),
       ('userWithCompany 2', 1),
       ('userWithCompany 3', 1)
on conflict do nothing;
