package com.example.hibernate.listeners;

import com.example.hibernate.BaseIT;
import com.example.hibernate.listeners.listener.entity.TeamWithListener;
import com.example.hibernate.listeners.listener.entity.UserTeamWithListener;
import com.example.hibernate.listeners.listener.entity.UserWithListener;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HibernateListenerIT extends BaseIT {

    @Test
    void prePersistHibernateListener() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserWithListener user = UserWithListener.builder()
                    .username("user1 with callback")
                    .build();

            assertNull(user.getCreatedAt());
            assertNull(user.getUpdatedAt());

            session.persist(user);
            session.getTransaction().commit();
            session.clear();


            var foundedUser = session.find(UserWithListener.class, user.getId());
            assertNotNull(foundedUser.getCreatedAt());
            assertNull(foundedUser.getUpdatedAt());
        }
    }

    @Test
    void postPersistHibernateListener() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserWithListener user = UserWithListener.builder()
                    .username("user2 with callback")
                    .build();

            TeamWithListener team = TeamWithListener.builder()
                    .name("team1")
                    .build();

            UserTeamWithListener userTeam1 = UserTeamWithListener.builder()
                    .createdBy("SYSTEM")
                    .joined(Instant.now())
                    .build();


            userTeam1.setUser(user);
            userTeam1.setTeam(team);

            assertEquals(0, team.getUserCount());

            session.persist(user);
            session.persist(team);
            session.persist(userTeam1);
            session.getTransaction().commit();
            session.clear();

            var foundedTeam = session.find(TeamWithListener.class, team.getId());
            assertEquals(1, foundedTeam.getUserCount());
        }
    }

    @Test
    void postRemoveHibernateListener() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserWithListener user1 = UserWithListener.builder()
                    .username("user3 with callback")
                    .build();
            UserWithListener user2 = UserWithListener.builder()
                    .username("user4 with callback")
                    .build();

            TeamWithListener team = TeamWithListener.builder()
                    .name("team2")
                    .build();

            UserTeamWithListener userTeam1 = UserTeamWithListener.builder()
                    .createdBy("SYSTEM")
                    .joined(Instant.now())
                    .build();
            UserTeamWithListener userTeam2 = UserTeamWithListener.builder()
                    .createdBy("SYSTEM")
                    .joined(Instant.now())
                    .build();


            userTeam1.setUser(user1);
            userTeam1.setTeam(team);
            userTeam2.setUser(user2);
            userTeam2.setTeam(team);

            session.persist(user1);
            session.persist(user2);
            session.persist(team);
            session.persist(userTeam1);
            session.persist(userTeam2);

            session.getTransaction().commit();
            session.clear();

            session.beginTransaction();
            var foundedTeam = session.find(TeamWithListener.class, team.getId());
            assertEquals(2, foundedTeam.getUserCount());

            session.remove(foundedTeam.getUserTeams().stream().findFirst().orElseThrow());
            session.flush();

            assertEquals(1, foundedTeam.getUserCount());
            session.getTransaction().commit();
        }
    }

    @Test
    void preUpdateHibernateListener() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserWithListener user = UserWithListener.builder()
                    .username("user5 with callback")
                    .build();

            session.persist(user);
            session.flush();
            session.clear();

            var foundedUser = session.find(UserWithListener.class, user.getId());
            assertNull(foundedUser.getUpdatedAt());

            var newUserName = "user6 with callback";
            foundedUser.setUsername(newUserName);
            var userAfterUpdate = session.merge(foundedUser);
            session.flush();
            assertNotNull(userAfterUpdate.getUpdatedAt());
        }
    }

}
