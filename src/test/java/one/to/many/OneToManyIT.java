package one.to.many;

import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Company;
import com.example.entity.User;
import com.example.helpers.MigrationHelper;
import com.example.many.to.one.UserWithManyToOneWithFetchLazy;
import com.example.one.to.many.*;
import itcontainers.ItContainers;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.TransientObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

@Slf4j
@Testcontainers
class OneToManyIT {
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
    void findOneEntity_whenFieldWithManyRelationNotExcludedFromToStringAndEqualsAndHashCodeMethods_thenStackOverflowError() {
        try (var session = sessionFactory.openSession()) {

            var company =
                    session.find(CompanyWithNoExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods.class, 1L);
            //noinspection ResultOfMethodCallIgnored
            Assertions.assertThrows(StackOverflowError.class, company::toString);
        }
    }

    @Test
    void findOneEntity_whenFieldWithManyRelationExcludedFromToStringAndEqualsAndHashCodeMethods_thenOk() {
        try (var session = sessionFactory.openSession()) {

            var company =
                    session.find(CompanyForOneToManyTests.class, 1L);
            Assertions.assertDoesNotThrow(company::toString);
        }
    }

    @Test
    void persist_whenManyEntitiesNotPersisted_thenHibernateSaveOnlyOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForOneToManyTests.builder()
                    .name("Default Company 1")
                    .build();
            var user = UserForOneToManyTests.builder()
                    .username("newUser 1")
                    .build();

            company.addUser(user);

            session.persist(company);
            Assertions.assertDoesNotThrow(transaction::commit);
            session.clear();
            Assertions.assertEquals(company, session.find(CompanyForOneToManyTests.class, company.getId()));
        }
    }


    @Test
    void whenOneToManyFetchLazy_thenHibernateDoNotAnyJoinToTableMappedByManyEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForOneToManyTests.builder()
                    .name("Default Company 2")
                    .build();
            var user = UserForOneToManyTests.builder()
                    .username("newUser 2")
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
    void whenOneToManyFetchEager_thenHibernateDoLeftJoinToTableMappedByManyEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyWithOneToManyWithFetchEager.builder()
                    .name("Default Company 3")
                    .build();
            var user = UserForOneToManyFetchEagerTests.builder()
                    .username("newUser 3")
                    .company(company)
                    .build();
            company.addUser(user);

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(CompanyWithOneToManyWithFetchEager.class, company.getId());
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            Assertions.assertTrue(query.contains("left join users"));
            System.setOut(originalOut);
        }
    }

    @Test
    void persistOneEntity_whenOneToManyCascadeTypePersis_thenHibernateSaveOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForOneToManyTests.builder()
                    .name("Default Company 4")
                    .build();
            var user = UserForOneToManyTests.builder()
                    .username("newUser 4")
                    .build();
            company.addUser(user);
            var users = company.getUsers();

            session.persist(company);
            transaction.commit();
            session.clear();

            var foundedCompany = session.find(CompanyForOneToManyTests.class, company.getId());
            Assertions.assertEquals(company, foundedCompany);
            //noinspection RedundantCast,rawtypes
            Assertions.assertEquals((Set) foundedCompany.getUsers(), (Set) users);
        }
    }

    @Test
    void persistManyEntity_whenManyToOneWithoutCascadeTypes_thenHibernateThrowsTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 5")
                    .build();
            var user = User.builder()
                    .username("newUser 5")
                    .company(company)
                    .build();

            session.persist(user);
            var exception = Assertions.assertThrows(IllegalStateException.class, transaction::commit);
            Assertions.assertEquals(TransientObjectException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void mergeOneEntity_whenOneToManyCascadeTypeMerge_thenHibernateSaveOrUpdateOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForOneToManyTests.builder()
                    .name("Default Company 6")
                    .build();
            var defaultUserKey = 1L;
            var user = session.find(UserForOneToManyTests.class, defaultUserKey);
            var oldUserCompany = user.getCompany();
            Assertions.assertNotEquals(oldUserCompany, company);
            company.addUser(user);

            company = session.merge(company);
            transaction.commit();
            session.clear();

            var foundedCompany = session.find(CompanyForOneToManyTests.class, company.getId());
            Assertions.assertEquals(company, foundedCompany);
            Assertions.assertNotNull(foundedCompany.getUsers());
            Assertions.assertFalse(foundedCompany.getUsers().isEmpty());
            var userFromFoundedCompany = company.getUsers().stream().findFirst().orElseThrow();
            Assertions.assertEquals(user, userFromFoundedCompany);
            Assertions.assertNotEquals(oldUserCompany, userFromFoundedCompany.getCompany());
            Assertions.assertEquals(company, userFromFoundedCompany.getCompany());
        }
    }

    @Test
    void mergeOneEntity_whenOneToManyCascadeTypeMerge_thenHibernateThrowsTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyWithoutCascadeTypesAndOrphanRemovalFalse.builder()
                    .name("Default Company 7")
                    .build();
            var defaultUserKey = 1L;
            var user = session.find(UserForOneToManyWithoutCascadeTypesAndOrphanRemovalFalse.class, defaultUserKey);
            company.addUser(user);

            session.merge(company);
            var exception = Assertions.assertThrows(IllegalStateException.class, transaction::commit);
            Assertions.assertEquals(TransientObjectException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void removeOneEntity_whenOneToManyCascadeTypeRemove_thenHibernateRemoveManyEntityBeforeOne() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForOneToManyTests.builder()
                    .name("Default Company 8")
                    .build();
            var user = UserForOneToManyTests.builder()
                    .username("newUser 8")
                    .build();
            company.addUser(user);

            session.persist(company);
            transaction.commit();
            session.clear();

            session.beginTransaction();
            Assertions.assertEquals(company, session.find(CompanyForOneToManyTests.class, company.getId()));
            Assertions.assertEquals(user, session.find(UserForOneToManyTests.class, user.getId()));
            session.clear();

            session.remove(company);
            session.getTransaction().commit();

            Assertions.assertNull(session.find(CompanyForOneToManyTests.class, company.getId()));
            Assertions.assertNull(session.find(UserForOneToManyTests.class, user.getId()));
        }
    }

    @Test
    void removeOneEntity_whenOneToManyCascadeTypeRemove_thenHibernateThrowsConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var company = CompanyWithoutCascadeTypesAndOrphanRemovalFalse.builder()
                    .name("Default Company 9")
                    .build();
            var user = UserForOneToManyWithoutCascadeTypesAndOrphanRemovalFalse.builder()
                    .username("newUser 9")
                    .build();
            company.addUser(user);

            session.persist(company);
            session.persist(user);
            session.getTransaction().commit();
            session.clear();

            var transaction = session.beginTransaction();
            session.remove(company);
            var exception = Assertions.assertThrows(PersistenceException.class, transaction::commit);
            Assertions.assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void refreshOneEntity_whenOneToManyCascadeTypeRefresh_thenHibernateRefreshOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var company = session.find(CompanyForOneToManyTests.class, 1L);
            var user = company.getUsers().stream().findFirst().orElseThrow();

            var newCompanyName = "new CompanyName";
            var oldCompanyName = company.getName();
            Assertions.assertNotEquals(newCompanyName, oldCompanyName);
            company.setName(newCompanyName);

            var oldUserName = user.getUsername();
            var newUsername = "new Username";
            user.setUsername(newUsername);


            session.refresh(company);
            transaction.commit();
            session.clear();

            Assertions.assertEquals(oldCompanyName, company.getName());
            Assertions.assertEquals(oldUserName, user.getUsername());
        }
    }

    @Test
    void refreshOneEntity_whenOneToManyCascadeTypeRefresh_thenHibernateRefreshOnlyOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var company = session.find(CompanyWithoutCascadeTypesAndOrphanRemovalFalse.class, 1L);
            var user = company.getUsers().stream().findFirst().orElseThrow();

            var newCompanyName = "new CompanyName";
            var oldCompanyName = company.getName();
            Assertions.assertNotEquals(newCompanyName, oldCompanyName);
            company.setName(newCompanyName);

            var oldUserName = user.getUsername();
            var newUsername = "new Username";
            user.setUsername(newUsername);


            session.refresh(company);
            transaction.commit();

            Assertions.assertEquals(oldCompanyName, company.getName());
            Assertions.assertEquals(newUsername, user.getUsername());
            Assertions.assertNotEquals(oldUserName, user.getUsername());
        }
    }

    @Test
    void detachOneEntity_whenOneToManyCascadeTypeDetach_thenHibernateDetachManyAndOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var company = session.find(CompanyForOneToManyTests.class, 1L);
            var user = company.getUsers().stream().findFirst().orElseThrow();

            Assertions.assertTrue(session.contains(company));
            Assertions.assertTrue(session.contains(user));

            session.detach(company);
            transaction.commit();

            Assertions.assertFalse(session.contains(company));
            Assertions.assertFalse(session.contains(user));
        }
    }

    @Test
    void detachManyEntity_whenManyToOneWithoutCascadeTypes_thenHibernateDetachOnlyManyEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var company = session.find(CompanyWithoutCascadeTypesAndOrphanRemovalFalse.class, 1L);
            var user = company.getUsers().stream().findFirst().orElseThrow();

            Assertions.assertTrue(session.contains(company));
            Assertions.assertTrue(session.contains(user));

            session.detach(company);
            transaction.commit();

            Assertions.assertFalse(session.contains(company));
            Assertions.assertTrue(session.contains(user));
        }
    }

    @Test
    void whenOneToManyOrphanRemovalTrue_thenHibernateDeleteObjectRelatedToCollectionUnderMany() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var companyId = 1L;
            var userId = 6L;

            var company = session.find(CompanyForOneToManyTests.class, companyId);
            var users = company.getUsers();
            Assertions.assertNotNull(users.stream()
                    .filter(user -> user.getId().equals(userId))
                    .findAny().orElseThrow());

            users.removeIf(user -> user.getId().equals(userId));

            session.getTransaction().commit();
            session.clear();

            var companyAfterCommit = session.find(CompanyForOneToManyTests.class, companyId);
            var usersAfterCommit = companyAfterCommit.getUsers();
            Assertions.assertTrue(usersAfterCommit.stream()
                    .filter(user -> user.getId().equals(userId))
                    .findAny().isEmpty());
        }
    }

    @Test
    void whenOneToManyOrphanRemovalFalse_thenHibernateDoNotDeleteObjectRelatedToCollectionUnderMany() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var companyId = 1L;
            var userId = 5L;

            var company = session.find(CompanyWithoutCascadeTypesAndOrphanRemovalFalse.class, companyId);
            var users = company.getUsers();
            Assertions.assertNotNull(users.stream()
                    .filter(user -> user.getId().equals(userId))
                    .findAny().orElseThrow());

            users.removeIf(user -> user.getId().equals(userId));

            session.getTransaction().commit();
            session.clear();

            var companyAfterCommit = session.find(CompanyWithoutCascadeTypesAndOrphanRemovalFalse.class, companyId);
            var usersAfterCommit = companyAfterCommit.getUsers();
            Assertions.assertNotNull(usersAfterCommit.stream()
                    .filter(user -> user.getId().equals(userId))
                    .findAny().orElseThrow());
        }
    }

}
