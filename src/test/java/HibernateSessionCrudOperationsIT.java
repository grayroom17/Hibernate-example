import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Birthday;
import com.example.entity.PersonalInfo;
import com.example.entity.User;
import com.example.helpers.MigrationHelper;
import itcontainers.ItContainers;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PersistentObjectException;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.example.entity.Role.USER;

@Slf4j
@Testcontainers
class HibernateSessionCrudOperationsIT {
    @Container
    public static final PostgreSQLContainer<?> POSTGRES = ItContainers.postgres();

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
    void find_whenDataIsSaved_thenReturnFoundedData() {
        try (var session = sessionFactory.openSession()) {
            var userId = 1L;
            var user = User.builder()
                    .id(userId)
                    .username("defaultUser")
                    .personalInfo(PersonalInfo.builder()
                            .firstname("Ivan")
                            .lastname("Ivanov")
                            .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                            .build())
                    .role(USER)
                    .info("""
                          {"age": 33, "name": "Ivan"}""")
                    .build();

            var foundedEntity = session.find(User.class, userId);
            Assertions.assertNotNull(foundedEntity);
            Assertions.assertEquals(user, foundedEntity);
        }
    }

    @Test
    void persist_whenDataIsNotSaved_thenSaveData() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var user = User.builder()
                    .username("newUser")
                    .personalInfo(PersonalInfo.builder()
                            .firstname("Иван")
                            .lastname("Иванов")
                            .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                            .build())
                    .role(USER)
                    .info("""
                          {
                          "name": "Ivan",
                          "age": 33
                          }
                          """)
                    .build();

            session.persist(user);
            transaction.commit();

            var foundedEntity = session.find(User.class, user.getId());
            Assertions.assertEquals(user, foundedEntity);
        }
    }

    @Test
    void persist_whenDataIsSavedButNotPersisted_thenThrowConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            var user = User.builder()
                    .id(1L)
                    .username("defaultUser")
                    .build();

            var transaction = session.beginTransaction();
            var message = "Converting `org.hibernate.PersistentObjectException` to JPA `PersistenceException` : detached entity passed to persist: com.example.entity.User";
            var exception = Assertions.assertThrows(PersistenceException.class, () -> session.persist(user), message);
            Assertions.assertEquals(PersistentObjectException.class, exception.getCause().getClass());
            var causeMessage = "detached entity passed to persist: com.example.entity.User";
            Assertions.assertEquals(causeMessage, exception.getCause().getMessage());
            transaction.rollback();
        }
    }

    @Test
    void persist_whenDataIsAlreadyPersisted_thenThrowEntityExistsException() {
        try (var session = sessionFactory.openSession()) {
            var userId = 1L;
            var persistedUser = session.find(User.class, userId);
            Assertions.assertNotNull(persistedUser);

            var user = User.builder()
                    .id(userId)
                    .username("defaultUser")
                    .build();

            var transaction = session.beginTransaction();
            var exceptionMessage = "A different object with the same identifier value was already associated with the session : [com.example.entity.UserManyToOne#defaultUser]";
            var exception = Assertions.assertThrows(PersistenceException.class, () -> session.persist(user), exceptionMessage);
            transaction.commit();
            Assertions.assertEquals(PersistentObjectException.class, exception.getCause().getClass());
        }
    }

    @Test
    void merge_whenDataIsNotSaved_thenSaveData() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var user = User.builder()
                    .username("notSavedUser")
                    .personalInfo(PersonalInfo.builder()
                            .firstname("Иван")
                            .lastname("Иванов")
                            .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                            .build())
                    .role(USER)
                    .info("""
                          {
                          "name": "Ivan",
                          "age": 33
                          }
                          """)
                    .build();
            user = session.merge(user);
            transaction.commit();

            var foundedEntity = session.find(User.class, user.getId());
            Assertions.assertEquals(user, foundedEntity);
        }
    }

    @Test
    void merge_whenDataIsSaved_thenUpdateData() {
        try (var session = sessionFactory.openSession()) {
            var userId = 3L;
            var user = User.builder()
                    .id(userId)
                    .username("userMustBeUpdated")
                    .personalInfo(PersonalInfo.builder()
                            .firstname("Петр")
                            .lastname("Петров")
                            .birthdate(new Birthday(LocalDate.of(1991, 1, 1)))
                            .build())
                    .role(USER)
                    .info("""
                          {
                          "name": "Petr",
                          "age": 32
                          }
                          """)
                    .build();
            var savedUser = session.find(User.class, userId);
            Assertions.assertNotNull(savedUser);
            Assertions.assertNotEquals(user, savedUser);

            var transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();

            var foundedEntity = session.find(User.class, userId);
            Assertions.assertEquals(user, foundedEntity);
        }
    }

    @Test
    void remove_whenDataIsAlreadyPersisted_thenThrowsEntityExistsException() {
        try (var session = sessionFactory.openSession()) {
            var userId = 2L;
            var user = User.builder()
                    .id(userId)
                    .username("userMustBeDeleted")
                    .build();

            var persistedUser = session.find(User.class, userId);
            Assertions.assertNotNull(persistedUser);
            Assertions.assertNotEquals(user, persistedUser);

            var transaction = session.beginTransaction();
            var message = "A different object with the same identifier value was already associated with the session : [com.example.entity.UserManyToOne#userMustBeDeleted]";
            Assertions.assertThrows(EntityExistsException.class, () -> session.remove(user), message);
            transaction.commit();
        }
    }

    @Test
    void remove_whenDataIsSaved_thenRemoveData() {
        try (var session = sessionFactory.openSession()) {
            var userId = 2L;
            var user = User.builder()
                    .id(userId)
                    .username("userMustBeDeleted")
                    .build();

            var persistedUser = session.find(User.class, userId);
            Assertions.assertNotNull(persistedUser);
            Assertions.assertNotEquals(user, persistedUser);
            session.evict(persistedUser);

            var transaction = session.beginTransaction();
            session.remove(user);
            transaction.commit();

            Assertions.assertNull(session.find(User.class, userId));
        }
    }

    @Test
    void remove_whenDataIsNotSaved_thenThrowStaleStateException() {
        try (var session = sessionFactory.openSession()) {
            var userId = 10_000_000L + (long) (Math.random() * (Long.MAX_VALUE - 10_000_000L));
            var user = User.builder()
                    .id(userId)
                    .username("notSavedUser")
                    .build();

            Assertions.assertNull(session.find(User.class, userId));

            var transaction = session.beginTransaction();
            Assertions.assertDoesNotThrow(() -> session.remove(user));
            var exception = Assertions.assertThrows(OptimisticLockException.class, transaction::commit);
            Assertions.assertEquals(StaleStateException.class, exception.getCause().getClass());
        }
    }
}