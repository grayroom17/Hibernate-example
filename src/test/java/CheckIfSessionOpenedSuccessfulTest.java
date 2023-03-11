import com.example.converter.BirthdayConverter;
import com.example.helpers.MigrationHelper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
class CheckIfSessionOpenedSuccessfulIT {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES = TestContainers.postgres();

    @BeforeAll
    public static void populateDb() {
        MigrationHelper.populateDb(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @Test
    void checkIfSessionOpenedSuccessful() {
        Configuration configuration = new Configuration();
        configuration.addAttributeConverter(BirthdayConverter.class,true);
//        configuration.registerTypeOverride(JsonBinaryType.class, new String[]{JsonBinaryType.INSTANCE.getName()});
        configuration.configure();
        try (var sessionFactory = configuration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            Assertions.assertNotNull(session);
        }
    }
}