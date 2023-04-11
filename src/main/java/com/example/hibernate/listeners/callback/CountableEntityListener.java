package com.example.hibernate.listeners.callback;

import com.example.hibernate.listeners.UserTeamWithListener;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;

public class CountableEntityListener {
    @PostPersist
    private void postPersist(UserTeamWithListener entity) {
        var team = entity.getTeam();
        team.setUserCount(team.getUserCount() + 1);

    }

    @PostRemove
    private void postRemove(UserTeamWithListener entity) {
        var team = entity.getTeam();
        team.setUserCount(team.getUserCount() - 1);
    }
}