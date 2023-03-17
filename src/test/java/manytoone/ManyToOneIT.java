package manytoone;

import com.example.config.SessionFactoryConfiguration;
import com.example.entity.*;
import com.example.entity.manytoone.UserWithManyToOneWithFetchLazy;
import com.example.entity.manytoone.UserWithManyToOneWithOptionalFalse;
import com.example.helpers.MigrationHelper;
import itcontainers.ItContainers;
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;

import static com.example.entity.Role.USER;

@Slf4j
@Testcontainers
class ManyToOneIT {
    @Container
    public static final PostgreSQLContainer<?> POSTGRES = ItContainers.postgres();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

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
                    .name("Default Company 1")
                    .build();
            var user = User.builder()
                    .username("newUser 1")
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
                    .name("Default Company 2")
                    .build();
            var user = User.builder()
                    .username("newUser 2")
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

    @Test
    void whenOneToManyOptionalTrueFetchEager_thenHibernateDoOuterLeftJoinToRelatedTable() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 3")
                    .build();
            var user = User.builder()
                    .username("newUser 3")
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


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(User.class, user.getId());
            log.warn(outContent.toString());
            Assertions.assertTrue(outContent.toString().contains("left join")
                                  || outContent.toString().contains("left outer join"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOneToManyOptionalFalseFetchEager_thenHibernateDoInnerJoinToRelatedTable() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 4")
                    .build();
            var user = UserWithManyToOneWithOptionalFalse.builder()
                    .username("newUser 4")
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


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(UserWithManyToOneWithOptionalFalse.class, user.getId());
            log.warn(outContent.toString());
            Assertions.assertTrue(outContent.toString().contains("join"));
            Assertions.assertFalse(outContent.toString().contains("left join"));
            Assertions.assertFalse(outContent.toString().contains("left outer join"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOneToManyOptionalFalseFetchLazy_thenHibernateDoNotAnyJoinToRelatedTable() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 5")
                    .build();
            var user = UserWithManyToOneWithFetchLazy.builder()
                    .username("newUser 5")
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


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(UserWithManyToOneWithFetchLazy.class, user.getId());
            log.warn(outContent.toString());
            Assertions.assertFalse(outContent.toString().contains("join"));
            System.setOut(originalOut);
        }
    }
}