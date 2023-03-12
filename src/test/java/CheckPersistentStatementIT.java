import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Birthday;
import com.example.entity.PersonalInfo;
import com.example.entity.User;
import com.example.helpers.MigrationHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.example.entity.Role.USER;

@Testcontainers
class CheckPersistentStatementIT {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES = TestContainers.postgres();

    @BeforeAll
    public static void initDb() {
        MigrationHelper.populateDb(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @Test
    void delete_whenDataIsNotPersisted_thenDataMadePersistedBeforeDelete() {
        try (var sessionFactory = SessionFactoryConfiguration.buildSessionFactory()) {
            var user = User.builder()
                    .username("someUser1")
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


            try (var session1 = sessionFactory.openSession()) {
                var transaction = session1.beginTransaction();
                session1.merge(user);
                transaction.commit();
            }


            try (var session2 = sessionFactory.openSession()) {
                var transaction = session2.beginTransaction();
                Assertions.assertDoesNotThrow(() -> session2.remove(user));
                transaction.commit();
                Assertions.assertNull(session2.find(User.class, user.getUsername()));
            }
        }
    }

    @Test
    void refresh_whenDataIsNotPersisted_thenDataWillBeUpdatedFromDb() {
        try (var sessionFactory = SessionFactoryConfiguration.buildSessionFactory()) {
            var user = User.builder()
                    .username("someUser2")
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
            var oldName = user.getPersonalInfo().getFirstname();


            try (var session1 = sessionFactory.openSession()) {
                var transaction = session1.beginTransaction();
                session1.merge(user);
                transaction.commit();
            }


            try (var session2 = sessionFactory.openSession()) {
                var transaction = session2.beginTransaction();
                var newName = "Василий";
                user.getPersonalInfo().setFirstname(newName);

                Assertions.assertDoesNotThrow(() -> session2.refresh(user));
                transaction.commit();
                Assertions.assertNotEquals(newName, user.getPersonalInfo().getFirstname());
                Assertions.assertEquals(oldName, user.getPersonalInfo().getFirstname());
            }
        }
    }

    @Test
    void merge_whenDataIsNotPersisted_thenDataWillNotBeUpdatedFromDb() {
        try (var sessionFactory = SessionFactoryConfiguration.buildSessionFactory()) {
            var user = User.builder()
                    .username("someUser2")
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
            var oldName = user.getPersonalInfo().getFirstname();


            try (var session1 = sessionFactory.openSession()) {
                var transaction = session1.beginTransaction();
                session1.merge(user);
                transaction.commit();
            }


            try (var session2 = sessionFactory.openSession()) {
                var transaction = session2.beginTransaction();
                var newName = "Василий";
                user.getPersonalInfo().setFirstname(newName);

                Assertions.assertDoesNotThrow(() -> session2.merge(user));
                transaction.commit();
                Assertions.assertNotEquals(oldName, user.getPersonalInfo().getFirstname());
                Assertions.assertEquals(newName, user.getPersonalInfo().getFirstname());
            }
        }
    }
}
