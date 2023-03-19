package primarykeytypes;

import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Birthday;
import com.example.entity.PersonalInfo;
import com.example.primarykeytypes.UserWithCompositePrimaryKey;
import com.example.helpers.MigrationHelper;
import itcontainers.ItContainers;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
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
class CheckUsersWithCompositePrimaryKeyIT {
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
    void merge() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var user = UserWithCompositePrimaryKey.builder()
                    .personalInfo(PersonalInfo.builder()
                            .firstname("Иван")
                            .lastname("Иванов")
                            .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                            .build())
                    .username("notSavedUser")
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

            var foundedEntity = session.find(UserWithCompositePrimaryKey.class, user.getPersonalInfo());
            Assertions.assertEquals(user, foundedEntity);
            Assertions.assertNotNull(foundedEntity.getPersonalInfo());
        }
    }
}