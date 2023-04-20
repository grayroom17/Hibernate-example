package com.example.hibernate.second_level_cache;

import com.example.hibernate.BaseIT;
import com.example.hibernate.config.SessionFactoryConfiguration;
import com.example.hibernate.converter.BirthdayConverter;
import com.example.hibernate.helpers.MigrationHelper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class SecondLevelCacheIT extends BaseIT {
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void initSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.addAttributeConverter(BirthdayConverter.class, true);
        configuration.registerTypeOverride(new JsonBinaryType(), new String[]{JsonBinaryType.INSTANCE.getName()});
        configuration.configure("second_level_cache/hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
    }

    @BeforeAll
    public static void initDbAndSessionFactory() {
        MigrationHelper.populateDb(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        sessionFactory = SessionFactoryConfiguration.buildSessionFactory();
    }

    @AfterAll
    public static void closeSessionFactory() {
        sessionFactory.close();
    }


    @Test
    void givenEntity_whenUseSecondLvlCache_thenEntityTakenOnlyOnceFromDataBase() {
        @SuppressWarnings("unused")
        User2ndLvlCache user = null;
        var userId = 10L;
        try (var session1 = sessionFactory.openSession()) {
            session1.beginTransaction();

            System.setOut(new PrintStream(outContent));
            //noinspection UnusedAssignment
            user = session1.find(User2ndLvlCache.class, userId);
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users"));
            outContent.reset();


            session1.find(User2ndLvlCache.class, userId);
            log.info(outContent.toString());
            query = prepareQuery();
            assertFalse(query.contains("from users"));
            outContent.reset();

            session1.getTransaction().commit();
        }

        try (var session2 = sessionFactory.openSession()) {
            session2.beginTransaction();

            session2.find(User2ndLvlCache.class, userId);
            log.info(outContent.toString());
            var query = prepareQuery();
            assertFalse(query.contains("from users"));
            outContent.reset();

            session2.getTransaction().commit();
        }

        sessionFactory.getCache().evictAllRegions();
        System.setOut(originalOut);
    }

    @Test
    void givenAssociatedEntity_whenUseSecondLvlCacheAndCacheAnnotationAboveAssociatedEntityAndClass_thenAssociatedEntityTakenOnlyOnceFromDataBase() {
        User2ndLvlCache user;
        var userId = 10L;
        try (var session1 = sessionFactory.openSession()) {
            session1.beginTransaction();

            System.setOut(new PrintStream(outContent));
            user = session1.find(User2ndLvlCache.class, userId);
            //noinspection ResultOfMethodCallIgnored
            user.getCompany().getName();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("from company"));
            outContent.reset();


            var user1 = session1.find(User2ndLvlCache.class, userId);
            //noinspection ResultOfMethodCallIgnored
            user1.getCompany().getName();
            log.info(outContent.toString());
            query = prepareQuery();
            assertFalse(query.contains("from users")
                        || query.contains("from company"));
            outContent.reset();

            session1.getTransaction().commit();
        }

        try (var session2 = sessionFactory.openSession()) {
            session2.beginTransaction();

            var user2 = session2.find(User2ndLvlCache.class, userId);
            //noinspection ResultOfMethodCallIgnored
            user2.getCompany().getName();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertFalse(query.contains("from users")
                        || query.contains("from company"));
            outContent.reset();

            session2.getTransaction().commit();
        }

        sessionFactory.getCache().evictAllRegions();
        System.setOut(originalOut);
    }

    @Test
    void givenAssociatedCollection_whenUseSecondLvlCacheAndCacheAnnotationAboveCollectionAndClass_thenCollectionTakenOnlyOnceFromDataBase() {
        User2ndLvlCache user;
        var userId = 10L;
        try (var session1 = sessionFactory.openSession()) {
            session1.beginTransaction();

            System.setOut(new PrintStream(outContent));
            user = session1.find(User2ndLvlCache.class, userId);
            //noinspection ResultOfMethodCallIgnored
            user.getPayments().size();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users")
                       && query.contains("from payment"));
            outContent.reset();


            var user1 = session1.find(User2ndLvlCache.class, userId);
            //noinspection ResultOfMethodCallIgnored
            user1.getPayments().size();
            log.info(outContent.toString());
            query = prepareQuery();
            assertFalse(query.contains("from users")
                        || query.contains("from payment"));
            outContent.reset();

            session1.getTransaction().commit();
        }

        try (var session2 = sessionFactory.openSession()) {
            session2.beginTransaction();

            var user2 = session2.find(User2ndLvlCache.class, userId);
            //noinspection ResultOfMethodCallIgnored
            user2.getPayments().size();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertFalse(query.contains("from users")
                        || query.contains("from payment"));
            outContent.reset();

            session2.getTransaction().commit();
        }

        sessionFactory.getCache().evictAllRegions();
        System.setOut(originalOut);
    }
}
