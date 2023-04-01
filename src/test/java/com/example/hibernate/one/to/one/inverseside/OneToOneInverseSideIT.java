package com.example.hibernate.one.to.one.inverseside;

import com.example.hibernate.BaseIT;
import com.example.hibernate.one.to.one.stack.owerflow.UserForOneToOneNotExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods;
import com.example.hibernate.one.to.one.inverseside.cascade.types.ProfileForOneToOneInverseSideWithoutCascadeTypes;
import com.example.hibernate.one.to.one.inverseside.cascade.types.UserForOneToOneInverseSideWithoutCascadeTypes;
import com.example.hibernate.one.to.one.inverseside.fetch.type.lazy.UserForOneToOneInverseSideFetchLazy;
import com.example.hibernate.one.to.one.inverseside.optional.UserForOneToOneInverseSideOptionalFalse;
import com.example.hibernate.one.to.one.inverseside.orhan.removal.ProfileForOneToOneInverseSideWithOrphanRemovalTrue;
import com.example.hibernate.one.to.one.inverseside.orhan.removal.UserForOneToOneInverseSideOrphanRemovalTrue;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OneToOneInverseSideIT extends BaseIT {

    @Test
    void findInverseSideEntity_whenInverseSideNotExcludedFromToStringAndEqualsAndHashCodeMethods_thenStackOverflowError() {
        try (var session = sessionFactory.openSession()) {

            var user =
                    session.find(UserForOneToOneNotExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods.class, 7L);
            //noinspection ResultOfMethodCallIgnored
            assertThrows(StackOverflowError.class, user::toString);
        }
    }

    @Test
    void findInverseSideEntity_whenInverseSideExcludedFromToStringAndEqualsAndHashCodeMethods_thenOk() {
        try (var session = sessionFactory.openSession()) {

            var user =
                    session.find(UserForOneToOneInverseSideTests.class, 7L);
            assertDoesNotThrow(user::toString);
        }
    }

    @Test
    void whenOptionalTrue_thenHibernateDoOuterLeftJoinToOwningSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            session.find(UserForOneToOneInverseSideTests.class, 1L);
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            Assertions.assertTrue(query.contains("left join profile")
                                  || query.contains("left outer join profile"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOptionalFalse_thenHibernateDoInnerJoinToToOwningSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            session.find(UserForOneToOneInverseSideOptionalFalse.class, 1L);
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            Assertions.assertTrue(query.contains("join profile") || query.contains("inner join profile"));
            Assertions.assertFalse(query.contains("left join profile"));
            Assertions.assertFalse(query.contains("left outer join profile"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenInverseSideFetchLazy_thenHibernateDoSelectToOwningSideAndDoNotInitializeOwningSideLazy() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var user =
                    session.find(UserForOneToOneInverseSideFetchLazy.class, 7L);
            log.warn(outContent.toString());
            assertFalse(outContent.toString().contains("from users"));
            assertFalse(outContent.toString().contains("from profile"));
            assertFalse(user.getProfile() instanceof HibernateProxy);
            System.setOut(originalOut);
        }
    }

    @Test
    void whenInverseSideFetchEager_thenHibernateDoLeftJoinToTableRelatedToOwningSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var user = session.find(UserForOneToOneInverseSideTests.class, 7L);
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            assertTrue(query.contains("left join profile"));
            System.setOut(originalOut);
        }
    }

    @Test
    void persistInverseSideEntity_whenInverseSideCascadeTypePersis_thenHibernateSaveInverseSideBeforeOwning() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = UserForOneToOneInverseSideTests.builder()
                    .username("newUser 1")
                    .build();
            var profile = ProfileForOneToOneInverseSideTests.builder()
                    .language("DE")
                    .programmingLanguage("C++")
                    .build();
            user.setProfile(profile);

            session.persist(user);
            session.getTransaction().commit();
            session.clear();

            var foundedUser = session.find(UserForOneToOneInverseSideTests.class, user.getId());
            assertEquals(user, foundedUser);
            assertEquals(profile, foundedUser.getProfile());
        }
    }

    @Test
    void persistInverseSideEntity_whenInverseSideWithoutCascadeTypes_thenHibernateSaveOnlyInverseSideEntity() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = UserForOneToOneInverseSideWithoutCascadeTypes.builder()
                    .username("newUser 2")
                    .build();
            var profile = ProfileForOneToOneInverseSideWithoutCascadeTypes.builder()
                    .language("UA")
                    .programmingLanguage("C")
                    .build();
            user.setProfile(profile);

            assertDoesNotThrow(() -> session.persist(user));
            session.getTransaction().commit();
            session.clear();

            var foundedUser = session.find(UserForOneToOneInverseSideWithoutCascadeTypes.class, user.getId());
            assertNotNull(foundedUser);
            assertNull(foundedUser.getProfile());
        }
    }

    @Test
    void mergeInverseSideEntity_whenInverseSideCascadeTypePersis_thenHibernateSaveInverseSideBeforeOwning() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = UserForOneToOneInverseSideTests.builder()
                    .username("newUser 3")
                    .build();
            var profile = ProfileForOneToOneInverseSideTests.builder()
                    .language("KZ")
                    .programmingLanguage("Lua")
                    .build();
            user.setProfile(profile);

            user = session.merge(user);
            session.getTransaction().commit();
            session.clear();

            var foundedUser = session.find(UserForOneToOneInverseSideTests.class, user.getId());
            assertEquals(user, foundedUser);
            assertEquals(user.getProfile(), foundedUser.getProfile());
        }
    }

    @Test
    void mergeInverseSideEntity_whenInverseSideWithoutCascadeTypes_thenHibernateSavaOrUpdateOnlyInverseSideEntity() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = UserForOneToOneInverseSideWithoutCascadeTypes.builder()
                    .username("newUser 4")
                    .build();
            var profile = ProfileForOneToOneInverseSideWithoutCascadeTypes.builder()
                    .language("UZ")
                    .programmingLanguage("JavaScript")
                    .build();
            user.setProfile(profile);

            user = session.merge(user);
            session.getTransaction().commit();
            session.clear();

            var foundedUser = session.find(UserForOneToOneInverseSideWithoutCascadeTypes.class, user.getId());
            assertNotNull(foundedUser);
            assertNull(foundedUser.getProfile());
        }
    }

    @Test
    void removeInverseSideEntity_whenInverseSideCascadeTypeRemove_thenHibernateRemoveOwningSideBeforeInverse() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = UserForOneToOneInverseSideTests.builder()
                    .username("newUser 5")
                    .build();
            var profile = ProfileForOneToOneInverseSideTests.builder()
                    .language("BY")
                    .programmingLanguage("Python")
                    .build();
            user.setProfile(profile);

            session.persist(user);
            session.getTransaction().commit();
            session.clear();

            session.beginTransaction();
            assertEquals(user, session.find(UserForOneToOneInverseSideTests.class, user.getId()));
            assertEquals(profile, session.find(ProfileForOneToOneInverseSideTests.class, profile.getId()));
            session.clear();

            session.remove(user);
            session.getTransaction().commit();

            assertNull(session.find(UserForOneToOneInverseSideTests.class, user.getId()));
            assertNull(session.find(ProfileForOneToOneInverseSideTests.class, user.getProfile().getId()));
        }
    }

    @Test
    void removeInverseSideEntity_whenInverseSideWithoutCascadeTypes_thenHibernateThrowsConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = UserForOneToOneInverseSideWithoutCascadeTypes.builder()
                    .username("newUser 6")
                    .build();
            var profile = ProfileForOneToOneInverseSideWithoutCascadeTypes.builder()
                    .language("IT")
                    .programmingLanguage("Scala")
                    .build();
            user.setProfile(profile);

            session.persist(user);
            session.persist(profile);
            session.getTransaction().commit();
            session.clear();

            var transaction = session.beginTransaction();
            session.remove(user);
            var exception = assertThrows(PersistenceException.class, transaction::commit);
            assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void refreshInverseSideEntity_whenInverseSideCascadeTypeRefresh_thenHibernateRefreshBothSide() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var user = session.find(UserForOneToOneInverseSideTests.class, 7L);
            var profile = user.getProfile();

            var newUsername = "new Username 1";
            var oldUserName = user.getUsername();
            assertNotEquals(newUsername, oldUserName);
            user.setUsername(newUsername);

            var newProfileProgrammingLanguage = "Cobol";
            var oldProfileProgrammingLanguage = profile.getProgrammingLanguage();
            assertNotEquals(newProfileProgrammingLanguage, oldProfileProgrammingLanguage);
            profile.setProgrammingLanguage(newProfileProgrammingLanguage);

            session.refresh(user);
            session.getTransaction().commit();
            session.clear();

            assertEquals(oldUserName, user.getUsername());
            assertEquals(oldProfileProgrammingLanguage, profile.getProgrammingLanguage());
        }
    }

    @Test
    void refreshInverseSideEntity_whenInverseSideWithoutCascadeTypes_thenHibernateRefreshOnlyInverseSide() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var user = session.find(UserForOneToOneInverseSideWithoutCascadeTypes.class, 7L);
            var profile = user.getProfile();

            var newUsername = "new Username 2";
            var oldUserName = user.getUsername();
            assertNotEquals(newUsername, oldUserName);
            user.setUsername(newUsername);

            var newProfileProgrammingLanguage = "Fortran";
            var oldProfileProgrammingLanguage = profile.getProgrammingLanguage();
            assertNotEquals(newProfileProgrammingLanguage, oldProfileProgrammingLanguage);
            profile.setProgrammingLanguage(newProfileProgrammingLanguage);

            session.refresh(user);
            session.getTransaction().commit();

            assertEquals(oldUserName, user.getUsername());
            assertEquals(newProfileProgrammingLanguage, profile.getProgrammingLanguage());
            assertNotEquals(oldProfileProgrammingLanguage, profile.getProgrammingLanguage());
        }
    }

    @Test
    void detachInverseSideEntity_whenInverseSideCascadeTypeDetach_thenHibernateDetachBothSides() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var user = session.find(UserForOneToOneInverseSideTests.class, 7L);
            var profile = user.getProfile();

            assertTrue(session.contains(profile));
            assertTrue(session.contains(user));

            session.detach(user);
            transaction.commit();

            assertFalse(session.contains(profile));
            assertFalse(session.contains(user));
        }
    }

    @Test
    void detachInverseSideEntity_whenInverseSideWithoutCascadeTypes_thenHibernateDetachOnlyInverseSideEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var user = session.find(UserForOneToOneInverseSideWithoutCascadeTypes.class, 7L);
            var profile = user.getProfile();

            assertTrue(session.contains(profile));
            assertTrue(session.contains(user));

            session.detach(user);
            transaction.commit();

            assertFalse(session.contains(user));
            assertTrue(session.contains(profile));
        }
    }

    @Test
    void inverseSideEntityOrphanRemovalTrue_whenSetForInverseSideOtherOwningSideObject_thenHibernateDeleteOrphanedOwningSideObject() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var userId = 9L;
            var user = session.find(UserForOneToOneInverseSideOrphanRemovalTrue.class, userId);
            var profile = user.getProfile();
            assertNotNull(profile);
            var otherProfile = ProfileForOneToOneInverseSideWithOrphanRemovalTrue.builder()
                    .language("CH")
                    .programmingLanguage("Ruby")
                    .build();

            user.setProfile(otherProfile);

            session.getTransaction().commit();
            session.clear();

            assertNull(session.find(ProfileForOneToOneInverseSideWithOrphanRemovalTrue.class, profile.getId()));
        }
    }

    @Test
    void inverseSideEntityOrphanRemovalFalse_whenSetForInverseSideOtherOwningSideObject_thenHibernateTryInsertNewOwningSideEntityAndThrowsConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var userId = 8L;
            var user = session.find(UserForOneToOneInverseSideTests.class, userId);
            var profile = user.getProfile();
            assertNotNull(profile);
            var otherProfile = ProfileForOneToOneInverseSideTests.builder()
                    .language("IN")
                    .programmingLanguage("HTML")
                    .build();

            user.setProfile(otherProfile);

            var exception = assertThrows(PersistenceException.class, transaction::commit);
            assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

}