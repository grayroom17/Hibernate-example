package com.example.hibernate.second_level_cache;

import com.example.hibernate.BaseIT;
import com.example.hibernate.config.SessionFactoryConfiguration;
import com.example.hibernate.converter.BirthdayConverter;
import com.example.hibernate.helpers.MigrationHelper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.cache.CacheManager;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SecondLevelCacheIT extends BaseIT {

    private static SessionFactory sessionFactory;
    private static CacheManager cacheManager;

    @BeforeAll
    static void initSessionFactoryAndCacheManager() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.addAttributeConverter(BirthdayConverter.class, true);
        configuration.registerTypeOverride(new JsonBinaryType(), new String[]{JsonBinaryType.INSTANCE.getName()});
        configuration.configure("second_level_cache/hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();

        RegionFactory regionFactory = ((SessionFactoryImplementor) sessionFactory).getCache().getRegionFactory();
        JCacheRegionFactory ehcacheRegionFactory = (JCacheRegionFactory) regionFactory;
        cacheManager = ehcacheRegionFactory.getCacheManager();
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

    @AfterEach
    public void systemOutToOriginalOut() {
        System.setOut(originalOut);
        outContent.reset();
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
    }

    @Test
    void givenEhCacheConfig_whenUseAliasesForRegions_thenThenHibernateUseCustomRegionAliases() {
        try (var session1 = sessionFactory.openSession()) {
            session1.beginTransaction();

            session1.find(User2ndLvlCache.class, 10L);
            session1.find(Company2ndLvlCache.class, 1L);

            session1.getTransaction().commit();
        }

        assertNotNull(cacheManager.getCache("Users"));
        assertNotNull(cacheManager.getCache("Companies"));
        assertNull(cacheManager.getCache("NotExistedCache"));

        sessionFactory.getCache().evictAllRegions();
    }

    @Test
    void givenEhCacheConfig_whenUseTtlForRegions_thenThenHibernateUseCustomTtl() {
        var userId = 10L;
        try (var session1 = sessionFactory.openSession()) {
            session1.beginTransaction();

            System.setOut(new PrintStream(outContent));
            session1.find(User2ndLvlCache.class, userId);
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users"));
            outContent.reset();
            System.setOut(originalOut);

            session1.getTransaction().commit();
        }

        Awaitility.await().pollDelay(1, TimeUnit.SECONDS).until(() -> true);

        try (var session2 = sessionFactory.openSession()) {
            session2.beginTransaction();

            System.setOut(new PrintStream(outContent));
            session2.find(User2ndLvlCache.class, userId);
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("from users"));
            outContent.reset();

            session2.getTransaction().commit();
        }

        sessionFactory.getCache().evictAllRegions();
    }

}
