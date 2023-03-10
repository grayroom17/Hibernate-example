import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
class CheckIfSessionOpenedSuccessfulIT {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES = TestContainers.postgres();

    @Test
    void checkIfSessionOpenedSuccessful() {
        Configuration configuration = new Configuration();
        configuration.configure();
        try (var sessionFactory = configuration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            Assertions.assertNotNull(session);
        }
    }
}