package com.example.hibernate.performance;

import com.example.hibernate.BaseIT;
import com.example.hibernate.dao.CriteriaDao;
import com.example.hibernate.dao.QueryDslDao;
import com.example.hibernate.entity.User;
import com.example.hibernate.entity.UserTeam;
import com.example.hibernate.performance.batch_size.UserPerformanceWithBatchSize;
import com.example.hibernate.performance.fetch.UserPerformanceWithFetchModeSubselect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.graph.RootGraph;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class PerformanceIT extends BaseIT {

    private final CriteriaDao criteriaDao = CriteriaDao.getInstance();
    private final QueryDslDao queryDslDao = QueryDslDao.getInstance();

    @Test
    void givenEntityWithFetchEagerForSeveralFields_whenGetEntityWithFindOrGetMethods_thenHibernateDoOnlyOneSelectQueryWithSeveralJoins() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            session.find(UserPerformanceWithFetchEager.class, 10L);

            log.info(outContent.toString());
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

            log.info(outContent.toString());
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

            log.info(outContent.toString());
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

            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("select u1_0.id, u1_0.username from users u1_0 where 1=1")//select
                       && query.contains("from payment")
                       && query.contains("receiver_id in(select u1_0.id from users u1_0 where 1=1)"));//subselect
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntityWithFetchEagerForSeveralFields_whenGetEntityWithHqlQueryWithFetch_thenHibernateDoSelectOnCartesianProduct() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            var users = session.createQuery("select u from UserPerformanceWithFetchEager u " +
                                            "join fetch u.payments " +
                                            "join fetch u.company " +
                                            "where 1 = 1",
                            UserPerformanceWithFetchEager.class)
                    .list();

            log.info(outContent.toString());
            var query = prepareQuery();

            //prepare native query
            query = StringUtils.substringAfter(query, "Hibernate: ");
            query = StringUtils.substringBefore(query, "Hibernate: ");
            log.info(query);

            var rows = session.createNativeQuery(query, Object.class).list();

            log.info("selected users: {} , but selected rows: {}", users.size(), rows.size());
            assertTrue(rows.size() > users.size());

            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntity_whenGetEntityWithCriteriaQueryWithFetch_thenHibernateDoSelectOnCartesianProduct() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            var users = criteriaDao.getAllUsersAndFetchCompaniesAndPayments(session);

            log.info(outContent.toString());
            var query = prepareQuery();

            //prepare native query
            query = StringUtils.substringAfter(query, "Hibernate: ");
            query = StringUtils.substringBefore(query, "Hibernate: ");
            log.info(query);

            var rows = session.createNativeQuery(query, Object.class).list();

            log.info("selected users: {} , but selected rows: {}", users.size(), rows.size());
            assertTrue(rows.size() > users.size());

            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntity_whenGetEntityWithDslQueryWithFetch_thenHibernateDoSelectOnCartesianProduct() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));

            var users = queryDslDao.getAllUsersAndFetchCompaniesAndPayments(session);

            log.info(outContent.toString());
            var query = prepareQuery();

            //prepare native query
            query = StringUtils.substringAfter(query, "Hibernate: ");
            query = StringUtils.substringBefore(query, "Hibernate: ");
            log.info(query);

            var rows = session.createNativeQuery(query, Object.class).list();

            log.info("selected users: {} , but selected rows: {}", users.size(), rows.size());
            assertTrue(rows.size() > users.size());

            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntity_whenGetEntityWithFindOrGetMethodsUsingFetchProfile_thenHibernateDoSelectAccordingToFetchProfile() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            session.enableFetchProfile("withCompanyAndPayments");

            session.find(User.class, 10L);

            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("join company")
                       && query.contains("join payment"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntity_whenGetEntityWithFindUsingEntityGraph_thenHibernateDoSelectAccordingToEntityGraph() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            RootGraph<?> graph = session.getEntityGraph("graphWithAllFields");


            Map<String, Object> properties = Map.of(GraphSemantic.LOAD.getJakartaHintName(), graph);
            session.find(User.class, 10L, properties);

            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("join company")
                       && query.contains("join payment")
                       && query.contains("join profile")
                       && query.contains("join users_team")
                       && query.contains("join team"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntity_whenGetEntityWithHqlQueryUsingEntityGraph_thenHibernateDoSelectAccordingToEntityGraph() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            RootGraph<?> graph = session.getEntityGraph("graphWithAllFields");

            session.createQuery("select u from User u", User.class)
                    .setHint(GraphSemantic.LOAD.getJakartaHintName(), graph)
                    .list();

            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("join company")
                       && query.contains("join payment")
                       && query.contains("join profile")
                       && query.contains("join users_team")
                       && query.contains("join team"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void givenEntity_whenGetEntityWithHqlQueryUsingProgrammaticallyEntityGraph_thenHibernateDoSelectAccordingToEntityGraph() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            var graph = session.createEntityGraph(User.class);
            graph.addAttributeNodes("payments", "userTeams");
            var subGraph = graph.addSubgraph("userTeams", UserTeam.class);
            subGraph.addAttributeNodes("team");

            session.createQuery("select u from User u", User.class)
                    .setHint(GraphSemantic.LOAD.getJakartaHintName(), graph)
                    .list();

            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("join payment")
                       && query.contains("join users_team")
                       && query.contains("join team"));
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
