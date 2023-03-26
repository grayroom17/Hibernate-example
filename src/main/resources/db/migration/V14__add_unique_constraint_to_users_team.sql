alter table if exists users_team
    add constraint users_team_user_id_team_id_key unique (user_id, team_id);