package com.example.hibernate.performance;

import com.example.hibernate.BaseIT;
import com.example.hibernate.performance.batch_size.UserPerformanceWithBatchSize;
import com.example.hibernate.performance.fetch.UserPerformanceWithFetchModeSubselect;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class PerformanceIT extends BaseIT {


    @Test
    void givenEntityWithFetchEagerForSeveralFields_whenGetEntityWithFindOrGetMethods_thenHibernateDoOnlyOneSelectQueryWithSeveralJoins() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            session.find(UserPerformanceWithFetchEager.class, 10L);

            log.warn(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("join company")
                       && query.contains("join payment")
                       && query.contains("join users_team")
                       && query.contains("join team"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntityWithFetchEagerForSeveralFields_whenGetEntityWithHqlQuery_thenHibernateDoSelectForEachEntityOfMappedFields() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            session.createQuery("select u from UserPerformanceWithFetchEager u",
                            UserPerformanceWithFetchEager.class)
                    .list();

            log.warn(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("from company")
                       && query.contains("from payment")
                       && query.contains("from users_team"));
            assertEquals(14, countMatches(query, "from payment"));
            assertEquals(4, countMatches(query, "from company"));
            assertTrue(query.contains("receiver_id=?"));
            assertTrue(query.contains("user_id=?"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntityWithFetchEagerAndFetchSize_whenGetEntityWithHqlQuery_thenHibernateDoBatchSelectForSeveralEntitiesOfMappedFields() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            session.createQuery("select u from UserPerformanceWithBatchSize u",
                            UserPerformanceWithBatchSize.class)
                    .list();

            log.warn(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("from payment")
                       && query.contains("from company"));
            assertEquals(5, countMatches(query, "from payment"));
            assertTrue(query.contains("receiver_id in(?,?,?)"));
            assertTrue(query.contains("id in(?,?)"));//company by ids
            assertEquals(2, countMatches(query, "from company"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntityWithFetchEagerAndFetchModeSubselect_whenGetEntityWithHqlQuery_thenHibernateDoBatchSelectWithSubselectForEntitiesOfMappedFields() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            session.createQuery("select u from UserPerformanceWithFetchModeSubselect u where 1 = 1",
                            UserPerformanceWithFetchModeSubselect.class)
                    .list();

            log.warn(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("select u1_0.id, u1_0.username from users u1_0 where 1=1")//select
                       && query.contains("from payment")
                       && query.contains("receiver_id in(select u1_0.id from users u1_0 where 1=1)"));//subselect
            outContent.reset();
            System.setOut(originalOut);
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
