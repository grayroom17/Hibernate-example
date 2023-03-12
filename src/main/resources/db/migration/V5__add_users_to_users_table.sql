insert into users (username, firstname, lastname, birth_date, role, info)
values ('defaultUser', 'Ivan', 'Ivanov', '1990-01-01', 'USER',
        '{
          "age": 33,
          "name": "Ivan"
        }')
on conflict do nothing;

insert into users (username, firstname, lastname, birth_date, role, info)
values ('userMustBeDeleted', 'Ivan', 'Ivanov', '1990-01-01', 'USER',
        '{
          "age": 33,
          "name": "Ivan"
        }')
on conflict do nothing;

insert into users (username, firstname, lastname, birth_date, role, info)
values ('userMustBeUpdated', 'Ivan', 'Ivanov', '1990-01-01', 'USER',
        '{
          "age": 33,
          "name": "Ivan"
        }')
on conflict do nothing;
