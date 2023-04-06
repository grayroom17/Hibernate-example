package com.example.hibernate.performance;

import com.example.hibernate.BaseIT;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

@Slf4j
class PerformanceIT extends BaseIT {


    @Test
    void givenEntityWithFetchEagerForSeveralFields_whenGetEntityWithFindOrGetMethods_thenHibernateDoOnlyOneSelectQueryWithSeveralJoins() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            session.find(UserPerformanceWithFetchEager.class, 10L);

            log.warn(outContent.toString());
            var query = prepareQuery();
            Assertions.assertTrue(query.contains("from users")
                                  && query.contains("join company")
                                  && query.contains("join payment")
                                  && query.contains("join users_team")
                                  && query.contains("join team"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntityWithFetchEagerForSeveralFields_whenGetEntityWithHqlQuery_thenHibernateDoSelectForEachEntityAndSeveralSelectsForMappedFields() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            session.createQuery("select u from UserPerformanceWithFetchEager u",
                            UserPerformanceWithFetchEager.class)
                    .list();

            log.warn(outContent.toString());
            var query = prepareQuery();
            Assertions.assertTrue(query.contains("from users")
                                  && query.contains("from company")
                                  && query.contains("from payment")
                                  && query.contains("from users_team"));
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
