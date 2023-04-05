insert into public.team (name)
values ('python');
insert into public.team (name)
values ('java');
insert into public.team (name)
values ('youtube-members');

insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'BillGates'),
        (select id from team where name = 'python'),
        '2023-04-05 08:50:45.229253', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'SteveJobs'),
        (select id from team where name = 'python'),
        '2023-04-05 08:50:45.233790', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'SergeyBrin'),
        (select id from team where name = 'python'),
        '2023-04-05 08:50:45.236541', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'BillGates'),
        (select id from team where name = 'java'),
        '2023-04-05 08:50:45.240256', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'SteveJobs'),
        (select id from team where name = 'java'),
        '2023-04-05 08:50:45.243100', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'TimCook'),
        (select id from team where name = 'java'),
        '2023-04-05 08:50:45.246375', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'DianeGreene'),
        (select id from team where name = 'java'),
        '2023-04-05 08:50:45.249350', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'BillGates'),
        (select id from team where name = 'youtube-members'),
        '2023-04-05 08:50:45.252202', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'SteveJobs'),
        (select id from team where name = 'youtube-members'),
        '2023-04-05 08:50:45.258426', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'TimCook'),
        (select id from team where name = 'youtube-members'),
        '2023-04-05 08:50:45.272043', 'SYSTEM');
insert into public.users_team (user_id, team_id, joined, created_by)
values ((select id from users where username = 'DianeGreene'),
        (select id from team where name = 'youtube-members'),
        '2023-04-05 08:50:45.275660', 'SYSTEM');

