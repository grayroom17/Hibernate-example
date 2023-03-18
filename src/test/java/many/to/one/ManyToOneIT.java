package many.to.one;

import com.example.config.SessionFactoryConfiguration;
import com.example.entity.*;
import com.example.entity.many.to.one.UserWithCascadeTypeAll;
import com.example.entity.many.to.one.UserWithManyToOneWithFetchLazy;
import com.example.entity.many.to.one.UserWithManyToOneWithOptionalFalse;
import com.example.helpers.MigrationHelper;
import itcontainers.ItContainers;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.TransientObjectException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;

@Slf4j
@Testcontainers
class ManyToOneIT {
    @Container
    public static final PostgreSQLContainer<?> POSTGRES = ItContainers.postgres();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private static SessionFactory sessionFactory;


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
    void persist_whenRelatedDataNotPersisted_thenTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 1")
                    .build();
            var user = User.builder()
                    .username("newUser 1")
                    .company(company)
                    .build();

            session.persist(user);
            var exception = Assertions.assertThrows(IllegalStateException.class, transaction::commit);
            Assertions.assertEquals(TransientObjectException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void persist_whenRelatedDataAlreadyPersisted_thenOk() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 2")
                    .build();
            var user = User.builder()
                    .username("newUser 2")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();

            var foundedEntity = session.find(User.class, user.getId());
            Assertions.assertEquals(user, foundedEntity);
            var foundedCompany = session.find(Company.class, foundedEntity.getCompany().getId());
            Assertions.assertEquals(company, foundedCompany);
        }
    }

    @Test
    void whenManyToOneOptionalTrueFetchEager_thenHibernateDoOuterLeftJoinToRelatedTable() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 3")
                    .build();
            var user = User.builder()
                    .username("newUser 3")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(User.class, user.getId());
            log.warn(outContent.toString());
            Assertions.assertTrue(outContent.toString().contains("left join")
                                  || outContent.toString().contains("left outer join"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenManyToOneOptionalFalseFetchEager_thenHibernateDoInnerJoinToRelatedTable() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 4")
                    .build();
            var user = UserWithManyToOneWithOptionalFalse.builder()
                    .username("newUser 4")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(UserWithManyToOneWithOptionalFalse.class, user.getId());
            log.warn(outContent.toString());
            Assertions.assertTrue(outContent.toString().contains("join"));
            Assertions.assertFalse(outContent.toString().contains("left join"));
            Assertions.assertFalse(outContent.toString().contains("left outer join"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenManyToOneOptionalFalseFetchLazy_thenHibernateDoNotAnyJoinToRelatedTable() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 5")
                    .build();
            var user = UserWithManyToOneWithFetchLazy.builder()
                    .username("newUser 5")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(UserWithManyToOneWithFetchLazy.class, user.getId());
            log.warn(outContent.toString());
            Assertions.assertFalse(outContent.toString().contains("join"));
            System.setOut(originalOut);
        }
    }

    @Test
    void persistManyEntity_whenManyToOneCascadeTypePersis_thenHibernateSaveOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 6")
                    .build();
            var user = UserWithCascadeTypeAll.builder()
                    .username("newUser 6")
                    .company(company)
                    .build();

            session.persist(user);
            transaction.commit();
            session.clear();

            var foundedUser = session.find(UserWithCascadeTypeAll.class, user.getId());
            Assertions.assertEquals(user, foundedUser);
            Assertions.assertEquals(company, foundedUser.getCompany());
        }
    }

    @Test
    void mergeManyEntity_whenManyToOneCascadeTypeMerge_thenHibernateSaveOrUpdateOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 7")
                    .build();
            var defaultUserKey = 1L;
            var user = session.find(UserWithCascadeTypeAll.class, defaultUserKey);
            user.setCompany(company);

            session.merge(user);
            transaction.commit();
            session.clear();

            var foundedUser = session.find(UserWithCascadeTypeAll.class, user.getId());
            Assertions.assertEquals(user, foundedUser);
            Assertions.assertNotNull(foundedUser.getCompany());
            Assertions.assertNotNull(foundedUser.getCompany().getId());
            Assertions.assertEquals(company.getName(), foundedUser.getCompany().getName());
        }
    }

    @Test
    void removeManyEntity_whenManyToOneCascadeTypeRemove_thenHibernateRemoveManyEntityBeforeOne() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 8")
                    .build();
            var user = UserWithCascadeTypeAll.builder()
                    .username("newUser 8")
                    .company(company)
                    .build();

            session.persist(user);
            transaction.commit();
            session.clear();

            session.beginTransaction();
            Assertions.assertEquals(user, session.find(UserWithCascadeTypeAll.class, user.getId()));
            session.clear();

            session.remove(user);
            session.getTransaction().commit();

            Assertions.assertNull(session.find(UserWithCascadeTypeAll.class, user.getId()));
        }
    }

    @Test
    void refreshManyEntity_whenManyToOneCascadeTypeRefresh_thenHibernateRefreshOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var userWithCompany = session.find(UserWithCascadeTypeAll.class, 4L);
            var company = userWithCompany.getCompany();

            Assertions.assertNull(userWithCompany.getPersonalInfo());
            Assertions.assertNull(userWithCompany.getRole());
            userWithCompany.setPersonalInfo(PersonalInfo.builder()
                    .firstname("Ivan")
                    .lastname("Ivanov")
                    .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                    .build());
            userWithCompany.setRole(Role.ADMIN);

            var companyName = company.getName();
            company.setName(companyName + "Changed");


            session.refresh(userWithCompany);
            transaction.commit();
            session.clear();

            Assertions.assertNull(userWithCompany.getPersonalInfo());
            Assertions.assertNull(userWithCompany.getRole());
            Assertions.assertEquals(companyName,userWithCompany.getCompany().getName());
        }
    }

    @Test
    void detachManyEntity_whenManyToOneCascadeTypeDetach_thenHibernateDetachManyAndOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var userWithCompany = session.find(UserWithCascadeTypeAll.class, 4L);
            var company = userWithCompany.getCompany();

            Assertions.assertTrue(session.contains(userWithCompany));
            Assertions.assertTrue(session.contains(company));

            session.detach(userWithCompany);
            transaction.commit();

            Assertions.assertFalse(session.contains(userWithCompany));
            Assertions.assertFalse(session.contains(company));
        }
    }

}