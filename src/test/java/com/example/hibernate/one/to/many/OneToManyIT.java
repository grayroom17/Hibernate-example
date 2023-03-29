package com.example.hibernate.one.to.many;

import com.example.hibernate.BaseIT;
import com.example.hibernate.many.to.one.UserWithManyToOneWithFetchLazy;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OneToManyIT extends BaseIT {

    @Test
    void findOneEntity_whenFieldWithManyRelationNotExcludedFromToStringAndEqualsAndHashCodeMethods_thenStackOverflowError() {
        try (var session = sessionFactory.openSession()) {

            var company =
                    session.find(CompanyForOneToManyTestsWithNoExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods.class, 1L);
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
            assertEquals(company, session.find(CompanyForOneToManyTests.class, company.getId()));
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
            var company = CompanyForOneToManyTestsWithFetchEager.builder()
                    .name("Default Company 3")
                    .build();
            var user = UserForOneToManyTestsWithFetchEager.builder()
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
            var foundedEntity = session.find(CompanyForOneToManyTestsWithFetchEager.class, company.getId());
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
            assertEquals(company, foundedCompany);
            //noinspection RedundantCast,rawtypes
            assertEquals((Set) foundedCompany.getUsers(), (Set) users);
        }
    }

    @Test
    void persistInverseSide_whenInverseSideWithoutCascadeTypes_thenHibernateSaveOnlyInverseSide() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var company = CompanyForOneToManyWithoutCascadeTypes.builder()
                    .name("Default Company 5")
                    .build();
            var user = UserForOneToManyInverseSideWithoutCascadeTypes.builder()
                    .username("newUser 5")
                    .company(company)
                    .build();
            company.addUser(user);

            session.persist(company);
            session.getTransaction().commit();
            session.clear();

            var foundedCompany = session.find(CompanyForOneToManyWithoutCascadeTypes.class, company.getId());
            assertEquals(company, foundedCompany);
            assertFalse(company.getUsers().isEmpty());
            assertTrue(foundedCompany.getUsers().isEmpty());
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
            assertEquals(company, foundedCompany);
            Assertions.assertNotNull(foundedCompany.getUsers());
            Assertions.assertFalse(foundedCompany.getUsers().isEmpty());
            var userFromFoundedCompany = company.getUsers().stream().findFirst().orElseThrow();
            assertEquals(user, userFromFoundedCompany);
            Assertions.assertNotEquals(oldUserCompany, userFromFoundedCompany.getCompany());
            assertEquals(company, userFromFoundedCompany.getCompany());
        }
    }

    @Test
    void mergeOneEntity_whenOneToManyWithoutCascadeTypes_thenHibernateThrowsTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.builder()
                    .name("Default Company 7")
                    .build();
            var defaultUserKey = 1L;
            var user = session.find(UserForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.class, defaultUserKey);
            company.addUser(user);

            session.merge(company);
            var exception = Assertions.assertThrows(IllegalStateException.class, transaction::commit);
            assertEquals(TransientObjectException.class, exception.getCause().getClass());
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
            assertEquals(company, session.find(CompanyForOneToManyTests.class, company.getId()));
            assertEquals(user, session.find(UserForOneToManyTests.class, user.getId()));
            session.clear();

            session.remove(company);
            session.getTransaction().commit();

            assertNull(session.find(CompanyForOneToManyTests.class, company.getId()));
            assertNull(session.find(UserForOneToManyTests.class, user.getId()));
        }
    }

    @Test
    void removeOneEntity_whenOneToManyWithoutCascadeTypes_thenHibernateThrowsConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var company = CompanyForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.builder()
                    .name("Default Company 9")
                    .build();
            var user = UserForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.builder()
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
            assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
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

            assertEquals(oldCompanyName, company.getName());
            assertEquals(oldUserName, user.getUsername());
        }
    }

    @Test
    void refreshOneEntity_whenOneToManyWithoutCascadeTypes_thenHibernateRefreshOnlyOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var company = session.find(CompanyForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.class, 1L);
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

            assertEquals(oldCompanyName, company.getName());
            assertEquals(newUsername, user.getUsername());
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

            var company = session.find(CompanyForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.class, 1L);
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
    void oneToManyOrphanRemovalTrue_whenDeleteObjectFromCollectionUnderMany_thenHibernateDeleteObjectRelatedToCollectionUnderMany() {
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
    void oneToManyOrphanRemovalFalse_whenDeleteObjectFromCollectionUnderMany_thenHibernateDoNotDeleteObjectRelatedToCollectionUnderMany() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var companyId = 1L;
            var userId = 5L;

            var company = session.find(CompanyForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.class, companyId);
            var users = company.getUsers();
            Assertions.assertNotNull(users.stream()
                    .filter(user -> user.getId().equals(userId))
                    .findAny().orElseThrow());

            users.removeIf(user -> user.getId().equals(userId));

            session.getTransaction().commit();
            session.clear();

            var companyAfterCommit = session.find(CompanyForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse.class, companyId);
            var usersAfterCommit = companyAfterCommit.getUsers();
            Assertions.assertNotNull(usersAfterCommit.stream()
                    .filter(user -> user.getId().equals(userId))
                    .findAny().orElseThrow());
        }
    }

}
