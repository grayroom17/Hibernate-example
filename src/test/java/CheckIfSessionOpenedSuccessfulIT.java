import com.example.config.SessionFactoryConfiguration;
import com.example.helpers.MigrationHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import itcontainers.ItContainers;

@Slf4j
@Testcontainers
class CheckIfSessionOpenedSuccessfulIT {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES = ItContainers.postgres();

    @BeforeAll
    public static void populateDb() {
        MigrationHelper.populateDb(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @Test
    void checkIfSessionOpenedSuccessful() {
        try (var sessionFactory = SessionFactoryConfiguration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            Assertions.assertNotNull(session);
        }
    }
}