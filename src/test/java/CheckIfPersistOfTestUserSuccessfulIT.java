import com.example.entity.User;
import com.example.helpers.MigrationHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;
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
class CheckIfPersistOfTestUserSuccessfulIT {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES = TestContainers.postgres();

    @BeforeAll
    public static void populateDb() {
        MigrationHelper.populateDb(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
    }

    @Test
    void checkIfPersistOfTestUserSuccessful() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.configure();
        try (var sessionFactory = configuration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var user = User.builder()
                    .username("grayroom")
                    .firstname("Сергей")
                    .lastname("Деев")
                    .birthDate(LocalDate.of(1991, 2, 17))
                    .age(32)
                    .role(USER)
                    .build();
            session.persist(user);
            transaction.commit();

            var foundedEntity = session.find(User.class, user.getUsername());
            Assertions.assertEquals(user, foundedEntity);
        }
    }
}