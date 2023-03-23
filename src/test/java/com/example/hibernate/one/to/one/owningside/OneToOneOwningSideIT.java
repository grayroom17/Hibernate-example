package com.example.hibernate.one.to.one.owningside;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.Company;
import com.example.hibernate.entity.User;
import com.example.hibernate.one.to.many.CompanyForOneToManyTests;
import com.example.hibernate.one.to.many.CompanyForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse;
import com.example.hibernate.one.to.many.UserForOneToManyTests;
import com.example.hibernate.one.to.many.UserForOneToManyTestsWithoutCascadeTypesAndOrphanRemovalFalse;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.Set;

@Slf4j
class OneToOneOwningSideIT extends BaseIT {

    @Test
    void findOwningSideEntity_whenInverseSideNotExcludedFromToStringAndEqualsAndHashCodeMethods_thenStackOverflowError() {
        try (var session = sessionFactory.openSession()) {

            var profile =
                    session.find(ProfileForOneToOneOwningSideTestsWithNotExcludedInverseSideFromToStringAndEqualsAndHashCodeMethods.class, 1L);
            //noinspection ResultOfMethodCallIgnored
            Assertions.assertThrows(StackOverflowError.class, profile::toString);
        }
    }

    @Test
    void findOwningSideEntity_whenInverseSideExcludedFromToStringAndEqualsAndHashCodeMethods_thenOk() {
        try (var session = sessionFactory.openSession()) {

            var profile =
                    session.find(ProfileForOneToOneOwningSideTests.class, 1L);
            Assertions.assertDoesNotThrow(profile::toString);
        }
    }


    @Test
    void whenOwningSideFetchLazy_thenHibernateDoNotAnyJoinToTableRelatedToInverseSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            var profileId = 1L;
            @SuppressWarnings("unused")
            var foundedEntity =
                    session.find(ProfileForOneToOneOwningSideTestsWithFetchLazy.class, profileId);
            log.warn(outContent.toString());
            Assertions.assertFalse(outContent.toString().contains("join"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOwningSideFetchEager_thenHibernateDoLeftJoinToTableRelatedToInverseSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            var profileId = 1L;
            @SuppressWarnings("unused")
            var foundedEntity = session.find(ProfileForOneToOneOwningSideTests.class, profileId);
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
    void persistOwningSideEntity_whenOwningSideCascadeTypePersis_thenHibernateSaveInverseSideBeforeOwning() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var profile = ProfileForOneToOneOwningSideTests.builder()
                    .language("DE")
                    .programmingLanguage("C++")
                    .build();
            var user = UserForOneToOneOwningSideTests.builder()
                    .username("newUser 1")
                    .build();
            user.setProfile(profile);

            session.persist(profile);
            session.getTransaction().commit();
            session.clear();

            var foundedProfile = session.find(ProfileForOneToOneOwningSideTests.class, profile.getId());
            Assertions.assertEquals(profile, foundedProfile);
            Assertions.assertEquals(user, foundedProfile.getUser());
        }
    }

    @Test
    void persistOwningSideEntity_whenOwningSideWithoutCascadeTypes_thenHibernateThrowsConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var profile = ProfileForOneToOneOwningSideTestsWithoutCascadeTypes.builder()
                    .language("UA")
                    .programmingLanguage("C")
                    .build();
            var user = UserForOneToOneOwningSideTestsWithoutCascadeTypes.builder()
                    .username("newUser 5")
                    .build();
            user.setProfile(profile);

            var exception = Assertions.assertThrows(PersistenceException.class,
                    () -> session.persist(profile));
            Assertions.assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void mergeOwningSideEntity_whenOwningSideCascadeTypePersis_thenHibernateSaveInverseSideBeforeOwning() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var profile = ProfileForOneToOneOwningSideTests.builder()
                    .language("KZ")
                    .programmingLanguage("Lua")
                    .build();
            var user = UserForOneToOneOwningSideTests.builder()
                    .username("newUser 3")
                    .build();
            user.setProfile(profile);

            profile = session.merge(profile);
            session.getTransaction().commit();
            session.clear();

            var foundedProfile = session.find(ProfileForOneToOneOwningSideTests.class, profile.getId());
            Assertions.assertEquals(profile, foundedProfile);
            Assertions.assertNotNull(foundedProfile.getUser());
            var userFromFoundedProfile = profile.getUser();
            Assertions.assertEquals(profile, userFromFoundedProfile.getProfile());
            Assertions.assertEquals(profile.getUser(), userFromFoundedProfile);
        }
    }

    @Test
    void mergeOneEntity_whenOneToManyCascadeTypeMerge_thenHibernateThrowsTransientObjectException() {
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