package com.example.hibernate.primarykeytypes.many.to.many.one_to_many_to_one;

import com.example.hibernate.BaseIT;
import com.example.hibernate.many.to.many.one_to_many_to_one.with_list.TeamForOneToManyToOneTests;
import com.example.hibernate.many.to.many.one_to_many_to_one.with_list.UserForOneToManyToOneTests;
import com.example.hibernate.many.to.many.one_to_many_to_one.with_list.UserTeamForOneToManyToOneTests;
import com.example.hibernate.many.to.many.one_to_many_to_one.with_set.TeamForOneToManyToOneTestsWithSet;
import com.example.hibernate.many.to.many.one_to_many_to_one.with_set.UserForOneToManyToOneTestsWithSet;
import com.example.hibernate.many.to.many.one_to_many_to_one.with_set.UserTeamForOneToManyToOneTestsWithSet;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class OneToManyToOneIT extends BaseIT {

    @Test
    void persist_whenUseSetAsCollection_thenHibernateDoLeftJoin() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = session.find(UserForOneToManyToOneTestsWithSet.class, 2L);
            var team = session.find(TeamForOneToManyToOneTestsWithSet.class, 1L);

            var userTeam = UserTeamForOneToManyToOneTestsWithSet.builder()
                    .joined(Instant.now())
                    .createdBy(user.getUsername())
                    .build();


            System.setOut(new PrintStream(outContent));
            userTeam.setUser(user);
            var query = prepareQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("left join team"));

            outContent.reset();
            userTeam.setTeam(team);
            query = prepareQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("left join users"));
            System.setOut(originalOut);

            session.persist(userTeam);
            session.getTransaction().commit();
            log.info("good");
        }
    }

    @Test
    void persist_whenUseListAsCollection_thenHibernateDoNotAnyJoin() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = session.find(UserForOneToManyToOneTests.class, 3L);
            var team = session.find(TeamForOneToManyToOneTests.class, 1L);

            var userTeam = UserTeamForOneToManyToOneTests.builder()
                    .joined(Instant.now())
                    .createdBy(user.getUsername())
                    .build();


            System.setOut(new PrintStream(outContent));
            userTeam.setUser(user);
            var query = prepareQuery();
            log.warn(outContent.toString());
            assertFalse(query.contains("join team"));

            outContent.reset();
            userTeam.setTeam(team);
            query = prepareQuery();
            log.warn(outContent.toString());
            assertFalse(query.contains("join users"));
            System.setOut(originalOut);

            session.persist(userTeam);
            session.getTransaction().commit();
            log.info("good");
        }
    }

    @NotNull
    private String prepareQuery() {
        return outContent.toString()
                .replaceAll("[\\t\\n\\r]+", " ")
                .replaceAll(" +", " ")
                .trim();
    }

}