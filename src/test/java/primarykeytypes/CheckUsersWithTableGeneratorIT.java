package primarykeytypes;

import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Birthday;
import com.example.entity.PersonalInfo;
import com.example.primarykeytypes.UserWithTableGenerator;
import com.example.helpers.MigrationHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import itcontainers.ItContainers;

import java.time.LocalDate;

import static com.example.entity.Role.USER;

@Slf4j
@Testcontainers
class CheckUsersWithTableGeneratorIT {
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
            var user = UserWithTableGenerator.builder()
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

            var foundedEntity = session.find(UserWithTableGenerator.class, user.getId());
            Assertions.assertEquals(user, foundedEntity);
            Assertions.assertNotNull(foundedEntity.getId());
        }
    }
}