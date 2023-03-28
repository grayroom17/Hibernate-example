package com.example.hibernate.primarykeytypes.many.to.many;

import com.example.hibernate.BaseIT;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ManyToManyWithCompositePrimaryKeyIT extends BaseIT {

    @Test
    void persist() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user1 = UserManyToManyWithCompositePrimaryKey.builder()
                    .username("User 1")
                    .build();
            var user2 = UserManyToManyWithCompositePrimaryKey.builder()
                    .username("User 2")
                    .build();
            var user3 = UserManyToManyWithCompositePrimaryKey.builder()
                    .username("User 3")
                    .build();

            var team1 = TeamManyToManyWithCompositePrimaryKey.builder()
                    .name("awesome idiots")
                    .build();
            var team2 = TeamManyToManyWithCompositePrimaryKey.builder()
                    .name("nerdy nerds")
                    .build();

            user1.addTeam(team1);
            user2.addTeam(team1);
            user3.addTeam(team1);

            user1.addTeam(team2);
            user3.addTeam(team2);

            session.persist(user1);
            session.persist(user2);
            session.persist(user3);

            session.getTransaction().commit();
            session.clear();


            var foundedUser1 = session.find(UserManyToManyWithCompositePrimaryKey.class, user1.getId());
            assertNotNull(foundedUser1);
            assertEquals(user1, foundedUser1);
            var foundedUser2 = session.find(UserManyToManyWithCompositePrimaryKey.class, user2.getId());
            assertNotNull(foundedUser2);
            assertEquals(user2, foundedUser2);
            var foundedUser3 = session.find(UserManyToManyWithCompositePrimaryKey.class, user3.getId());
            assertNotNull(foundedUser3);
            assertEquals(user3, foundedUser3);

            var foundedTeam1 = session.find(TeamManyToManyWithCompositePrimaryKey.class, team1.getId());
            assertNotNull(foundedTeam1);
            assertEquals(team1, foundedTeam1);
            var foundedTeam2 = session.find(TeamManyToManyWithCompositePrimaryKey.class, team2.getId());
            assertNotNull(foundedTeam2);
            assertEquals(team2, foundedTeam2);
        }
    }

    @Test
    void remove() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = (UserManyToManyWithCompositePrimaryKey) session.createSelectionQuery(
                            "select user " +
                            "from UserManyToManyWithCompositePrimaryKey user " +
                            "where user.username = 'User with teams'")
                    .getSingleResult();

            assertNotNull(user);
            var teams = user.getTeams();
            var teamsIds = teams.stream().map(TeamManyToManyWithCompositePrimaryKey::getId).toList();
            assertNotNull(teams);
            assertFalse(teams.isEmpty());

            teams.clear();

            session.getTransaction().commit();
            session.clear();

            var foundedUser = session.find(UserManyToManyWithCompositePrimaryKey.class, user.getId());
            assertEquals(user, foundedUser);
            assertTrue(teamsIds.stream().allMatch(teamId ->
                    session.find(TeamManyToManyWithCompositePrimaryKey.class, teamId) != null));
            assertTrue(teamsIds.stream().allMatch(teamId ->
                    session.find(TeamManyToManyWithCompositePrimaryKey.class, teamId).getUsers().isEmpty()));
        }
    }

}