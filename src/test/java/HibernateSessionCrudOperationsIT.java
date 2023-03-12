import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Birthday;
import com.example.entity.User;
import com.example.helpers.MigrationHelper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
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
    public static final PostgreSQLContainer<?> POSTGRES = TestContainers.postgres();

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
            var user = User.builder()
                    .username("defaultUser")
                    .firstname("Ivan")
                    .lastname("Ivanov")
                    .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                    .role(USER)
                    .info("""
                          {"age": 33, "name": "Ivan"}""")
                    .build();

            var foundedEntity = session.find(User.class, user.getUsername());
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
                    .firstname("Иван")
                    .lastname("Иванов")
                    .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                    .role(USER)
                    .info("""
                          {
                          "name": "Ivan",
                          "age": 33
                          }
                          """)
                    .build();
            Assertions.assertNull(session.find(User.class, user.getUsername()));

            session.persist(user);
            transaction.commit();

            var foundedEntity = session.find(User.class, user.getUsername());
            Assertions.assertEquals(user, foundedEntity);
        }
    }

    @Test
    void persist_whenDataIsSaved_thenThrowConstraintViolationException() {
        try (var session = sessionFactory.openSession()) {
            var username = "defaultUser";

            var user = User.builder()
                    .username(username)
                    .build();

            var transaction = session.beginTransaction();
            session.persist(user);
            var message = "Converting `org.hibernate.exception.ConstraintViolationException` to JPA `PersistenceException` : could not execute statement";
            var exception = Assertions.assertThrows(PersistenceException.class,
                    transaction::commit,
                    message);
            Assertions.assertEquals(ConstraintViolationException.class, exception.getCause().getClass());
        }
    }

    @Test
    void persist_whenDataIsAlreadyPersisted_thenThrowEntityExistsException() {
        try (var session = sessionFactory.openSession()) {
            var username = "defaultUser";
            var persistedUser = session.find(User.class, username);
            Assertions.assertNotNull(persistedUser);

            var user = User.builder()
                    .username(username)
                    .build();

            var transaction = session.beginTransaction();
            var exceptionMessage = "A different object with the same identifier value was already associated with the session : [com.example.entity.User#defaultUser]";
            Assertions.assertThrows(EntityExistsException.class, () -> session.persist(user), exceptionMessage);
            transaction.commit();
        }
    }

    @Test
    void merge_whenDataIsNotSaved_thenSaveData() {
        try (var session = sessionFactory.openSession()) {
            var username = "notSavedUser";
            Assertions.assertNull(session.find(User.class, username));

            var transaction = session.beginTransaction();
            var user = User.builder()
                    .username(username)
                    .firstname("Иван")
                    .lastname("Иванов")
                    .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                    .role(USER)
                    .info("""
                          {
                          "name": "Ivan",
                          "age": 33
                          }
                          """)
                    .build();
            session.merge(user);
            transaction.commit();

            var foundedEntity = session.find(User.class, username);
            Assertions.assertEquals(user, foundedEntity);
        }
    }

    @Test
    void merge_whenDataIsSaved_thenUpdateData() {
        try (var session = sessionFactory.openSession()) {
            var username = "userMustBeUpdated";
            var user = User.builder()
                    .username(username)
                    .firstname("Петр")
                    .lastname("Петров")
                    .birthdate(new Birthday(LocalDate.of(1991, 1, 1)))
                    .role(USER)
                    .info("""
                          {
                          "name": "Petr",
                          "age": 32
                          }
                          """)
                    .build();
            var savedUser = session.find(User.class, username);
            Assertions.assertNotNull(savedUser);
            Assertions.assertNotEquals(user, savedUser);

            var transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();

            var foundedEntity = session.find(User.class, username);
            Assertions.assertEquals(user, foundedEntity);
        }
    }

    @Test
    void remove_whenDataIsAlreadyPersisted_thenThrowsEntityExistsException() {
        try (var session = sessionFactory.openSession()) {
            var username = "userMustBeDeleted";
            var user = User.builder()
                    .username(username)
                    .build();

            var persistedUser = session.find(User.class, username);
            Assertions.assertNotNull(persistedUser);
            Assertions.assertNotEquals(user, persistedUser);

            var transaction = session.beginTransaction();
            var message = "A different object with the same identifier value was already associated with the session : [com.example.entity.User#userMustBeDeleted]";
            Assertions.assertThrows(EntityExistsException.class, () -> session.remove(user), message);
            transaction.commit();
        }
    }

    @Test
    void remove_whenDataIsSaved_thenRemoveData() {
        try (var session = sessionFactory.openSession()) {
            var username = "userMustBeDeleted";
            var user = User.builder()
                    .username(username)
                    .build();

            var persistedUser = session.find(User.class, username);
            Assertions.assertNotNull(persistedUser);
            Assertions.assertNotEquals(user, persistedUser);
            session.evict(persistedUser);

            var transaction = session.beginTransaction();
            session.remove(user);
            transaction.commit();

            Assertions.assertNull(session.find(User.class, username));
        }
    }

    @Test
    void remove_whenDataIsNotSaved_thenOk() {
        try (var session = sessionFactory.openSession()) {
            var username = "notSavedUser";
            var user = User.builder()
                    .username(username)
                    .build();

            Assertions.assertNull(session.find(User.class, username));

            var transaction = session.beginTransaction();
            Assertions.assertDoesNotThrow(() -> session.remove(user));
            Assertions.assertDoesNotThrow(transaction::commit);
        }
    }
}