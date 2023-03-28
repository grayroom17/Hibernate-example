package com.example.hibernate.one.to.one.owningside;

import com.example.hibernate.BaseIT;
import com.example.hibernate.one.to.one.stack.owerflow.ProfileForOneToOneWithNotExcludedInverseSideFromToStringAndEqualsAndHashCodeMethods;
import com.example.hibernate.one.to.one.owningside.cascade.type.ProfileForOneToOneOwningSideTestsWithoutCascadeTypes;
import com.example.hibernate.one.to.one.owningside.cascade.type.UserForOneToOneOwningSideTestsWithoutCascadeTypes;
import com.example.hibernate.one.to.one.owningside.fetch.type.lazy.ProfileForOneToOneOwningSideTestsWithFetchLazy;
import com.example.hibernate.one.to.one.owningside.optional.ProfileForOneToOneOwningSideWithOptionalFalse;
import com.example.hibernate.one.to.one.owningside.orphan.removal.ProfileForOneToOneOwningSideOrphanRemovalFalse;
import com.example.hibernate.one.to.one.owningside.orphan.removal.UserForOneToOneOwningSideOrphanRemovalFalse;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OneToOneOwningSideIT extends BaseIT {

    @Test
    void findOwningSideEntity_whenInverseSideNotExcludedFromToStringAndEqualsAndHashCodeMethods_thenStackOverflowError() {
        try (var session = sessionFactory.openSession()) {

            var profile =
                    session.find(ProfileForOneToOneWithNotExcludedInverseSideFromToStringAndEqualsAndHashCodeMethods.class, 1L);
            //noinspection ResultOfMethodCallIgnored
            assertThrows(StackOverflowError.class, profile::toString);
        }
    }

    @Test
    void findOwningSideEntity_whenInverseSideExcludedFromToStringAndEqualsAndHashCodeMethods_thenOk() {
        try (var session = sessionFactory.openSession()) {
            var profile =
                    session.find(ProfileForOneToOneOwningSideTests.class, 1L);
            assertDoesNotThrow(profile::toString);
        }
    }

    @Test
    void whenOptionalTrue_thenHibernateDoOuterLeftJoinToInverseSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            session.find(ProfileForOneToOneOwningSideTests.class, 1L);
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            Assertions.assertTrue(query.contains("left join users")
                                  || query.contains("left outer join users"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOptionalFalse_thenHibernateDoInnerJoinToInverseSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            session.find(ProfileForOneToOneOwningSideWithOptionalFalse.class, 1L);
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            Assertions.assertTrue(query.contains("join users") || query.contains("inner join users"));
            Assertions.assertFalse(query.contains("left join users"));
            Assertions.assertFalse(query.contains("left outer join users"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOwningSideFetchLazy_thenHibernateDoNotAnyJoinToTableRelatedToInverseSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity =
                    session.find(ProfileForOneToOneOwningSideTestsWithFetchLazy.class, 1L);
            log.warn(outContent.toString());
            assertFalse(outContent.toString().contains("join"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOwningSideFetchEager_thenHibernateDoLeftJoinToTableRelatedToInverseSide() {
        try (var session = sessionFactory.openSession()) {
            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(ProfileForOneToOneOwningSideTests.class, 1L);
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            assertTrue(query.contains("left join users"));
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
            assertEquals(profile, foundedProfile);
            assertEquals(user, foundedProfile.getUser());
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
                    .username("newUser 2")
                    .build();
            user.setProfile(profile);

            var exception = assertThrows(PersistenceException.class,
                    () -> session.persist(profile));
            assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
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
            assertEquals(profile, foundedProfile);
            assertEquals(profile.getUser(), foundedProfile.getUser());
        }
    }

    @Test
    void mergeOwningSideEntity_whenOwningSideWithoutCascadeTypes_thenHibernateThrowsConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var profile = ProfileForOneToOneOwningSideTestsWithoutCascadeTypes.builder()
                    .language("UZ")
                    .programmingLanguage("JavaScript")
                    .build();
            var user = UserForOneToOneOwningSideTestsWithoutCascadeTypes.builder()
                    .username("newUser 4")
                    .build();
            user.setProfile(profile);

            var exception = assertThrows(PersistenceException.class,
                    () -> session.merge(profile));
            assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void removeOwningSideEntity_whenOwningSideCascadeTypeRemove_thenHibernateRemoveOwningSideBeforeInverse() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var profile = ProfileForOneToOneOwningSideTests.builder()
                    .language("BY")
                    .programmingLanguage("Python")
                    .build();
            var user = UserForOneToOneOwningSideTests.builder()
                    .username("newUser 5")
                    .build();
            user.setProfile(profile);

            session.persist(profile);
            session.getTransaction().commit();
            session.clear();

            session.beginTransaction();
            assertEquals(profile, session.find(ProfileForOneToOneOwningSideTests.class, profile.getId()));
            assertEquals(user, session.find(UserForOneToOneOwningSideTests.class, user.getId()));
            session.clear();

            session.remove(profile);
            session.getTransaction().commit();

            assertNull(session.find(ProfileForOneToOneOwningSideTests.class, profile.getId()));
            assertNull(session.find(UserForOneToOneOwningSideTests.class, profile.getUser().getId()));
        }
    }

    @Test
    void removeOwningSideEntity_whenOwningSideWithoutCascadeTypes_thenHibernateDeleteOnlyOwningSideEntity() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var profile = ProfileForOneToOneOwningSideTestsWithoutCascadeTypes.builder()
                    .language("IT")
                    .programmingLanguage("Scala")
                    .build();
            var user = UserForOneToOneOwningSideTestsWithoutCascadeTypes.builder()
                    .username("newUser 6")
                    .build();
            user.setProfile(profile);

            session.persist(user);
            session.persist(profile);
            session.getTransaction().commit();
            session.clear();

            session.beginTransaction();
            session.remove(profile);
            session.getTransaction().commit();
            assertNull(session.find(ProfileForOneToOneOwningSideTestsWithoutCascadeTypes.class, profile.getId()));
            assertNotNull(session.find(UserForOneToOneOwningSideTestsWithoutCascadeTypes.class, user.getId()));
        }
    }

    @Test
    void refreshOwningSideEntity_whenOwningSideCascadeTypeRefresh_thenHibernateRefreshBothSide() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var profile = session.find(ProfileForOneToOneOwningSideTests.class, 2L);
            var user = profile.getUser();

            var newProfileProgrammingLanguage = "Cobol";
            var oldProfileProgrammingLanguage = profile.getProgrammingLanguage();
            assertNotEquals(newProfileProgrammingLanguage, oldProfileProgrammingLanguage);
            profile.setProgrammingLanguage(newProfileProgrammingLanguage);

            var newUsername = "new Username";
            var oldUserName = user.getUsername();
            assertNotEquals(newUsername, oldUserName);
            user.setUsername(newUsername);

            session.refresh(profile);
            session.getTransaction().commit();
            session.clear();

            assertEquals(oldProfileProgrammingLanguage, profile.getProgrammingLanguage());
            assertEquals(oldUserName, user.getUsername());
        }
    }

    @Test
    void refreshOwningSideEntity_whenOwningSideWithoutCascadeTypes_thenHibernateRefreshOnlyOwningSide() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var profile = session.find(ProfileForOneToOneOwningSideTestsWithoutCascadeTypes.class, 1L);
            var user = profile.getUser();

            var newProfileProgrammingLanguage = "Cobol";
            var oldProfileProgrammingLanguage = profile.getProgrammingLanguage();
            assertNotEquals(newProfileProgrammingLanguage, oldProfileProgrammingLanguage);
            profile.setProgrammingLanguage(newProfileProgrammingLanguage);

            var newUsername = "new Username";
            var oldUserName = user.getUsername();
            assertNotEquals(newUsername, oldUserName);
            user.setUsername(newUsername);

            session.refresh(profile);
            session.getTransaction().commit();

            assertEquals(oldProfileProgrammingLanguage, profile.getProgrammingLanguage());
            assertEquals(newUsername, user.getUsername());
            assertNotEquals(oldUserName, user.getUsername());
        }
    }

    @Test
    void detachOwningSideEntity_whenOwningSideCascadeTypeDetach_thenHibernateDetachBothSides() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var profile = session.find(ProfileForOneToOneOwningSideTests.class, 1L);
            var user = profile.getUser();

            assertTrue(session.contains(profile));
            assertTrue(session.contains(user));

            session.detach(profile);
            transaction.commit();

            assertFalse(session.contains(profile));
            assertFalse(session.contains(user));
        }
    }

    @Test
    void detachOwningSideEntity_whenOwningSideWithoutCascadeTypes_thenHibernateDetachOnlyOwningSideEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var profile = session.find(ProfileForOneToOneOwningSideTestsWithoutCascadeTypes.class, 1L);
            var user = profile.getUser();

            assertTrue(session.contains(profile));
            assertTrue(session.contains(user));

            session.detach(profile);
            transaction.commit();

            assertFalse(session.contains(profile));
            assertTrue(session.contains(user));
        }
    }

    @Test
    void owningSideEntityOrphanRemovalTrue_whenSetForOwningSideOtherInverseSideObject_thenHibernateDeleteOrphanedInverseSideObject() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var profileId = 3L;
            var profile = session.find(ProfileForOneToOneOwningSideTests.class, profileId);
            var user = profile.getUser();
            assertNotNull(user);
            var oldUserId = user.getId();
            var otherUser = session.find(UserForOneToOneOwningSideTests.class, 1L);

            profile.setUser(otherUser);

            session.getTransaction().commit();
            session.clear();

            assertNull(session.find(UserForOneToOneOwningSideTests.class, oldUserId));
        }
    }

    @Test
    void owningSideEntityOrphanRemovalFalse_whenSetForOwningSideOtherInverseSideObject_thenHibernateDoNotDeleteOrphanedInverseSideObject() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var profileId = 1L;
            var profile = session.find(ProfileForOneToOneOwningSideOrphanRemovalFalse.class, profileId);
            var user = profile.getUser();
            assertNotNull(user);
            var oldUserId = user.getId();
            var otherUser = session.find(UserForOneToOneOwningSideOrphanRemovalFalse.class, 2L);

            profile.setUser(otherUser);

            session.getTransaction().commit();
            session.clear();

            var foundedUser = session.find(UserForOneToOneOwningSideOrphanRemovalFalse.class, oldUserId);
            assertNotNull(foundedUser);
        }
    }

}