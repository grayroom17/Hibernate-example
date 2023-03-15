import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Birthday;
import com.example.entity.Company;
import com.example.entity.PersonalInfo;
import com.example.entity.User;
import com.example.helpers.MigrationHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.TransientObjectException;
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
class CheckManyToOneIT {
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
    void persist_whenRelatedDataNotPersisted_thenTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company")
                    .build();
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
                    .company(company)
                    .build();

            session.persist(user);
            var exception = Assertions.assertThrows(IllegalStateException.class, transaction::commit);
            Assertions.assertEquals(TransientObjectException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void persist_whenRelatedDataAlreadyPersisted_thenOk() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company")
                    .build();
            var user = User.builder()
                    .username("newUser")
                    .personalInfo(PersonalInfo.builder()
                            .firstname("Иван")
                            .lastname("Иванов")
                            .birthdate(new Birthday(LocalDate.of(1990, 1, 1)))
                            .build())
                    .role(USER)
                    .info("""
                          {"age": 33, "name": "Ivan"}""")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();

            var foundedEntity = session.find(User.class, user.getId());
            Assertions.assertEquals(user, foundedEntity);
            var foundedCompany = session.find(Company.class, foundedEntity.getCompany().getId());
            Assertions.assertEquals(company, foundedCompany);
        }
    }
}