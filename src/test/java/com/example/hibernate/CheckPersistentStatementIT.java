package com.example.hibernate;

import com.example.hibernate.config.SessionFactoryConfiguration;
import com.example.hibernate.entity.Birthday;
import com.example.hibernate.entity.PersonalInfo;
import com.example.hibernate.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.example.hibernate.entity.Role.USER;

@Testcontainers
class CheckPersistentStatementIT extends BaseIT {

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
                user = session1.merge(user);
                transaction.commit();
            }


            try (var session2 = sessionFactory.openSession()) {
                var transaction = session2.beginTransaction();
                User finalUser = user;
                Assertions.assertDoesNotThrow(() -> session2.remove(finalUser));
                transaction.commit();
                Assertions.assertNull(session2.find(User.class, user.getId()));
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
                user = session1.merge(user);
                transaction.commit();
            }


            try (var session2 = sessionFactory.openSession()) {
                var transaction = session2.beginTransaction();
                var newName = "Василий";
                user.getPersonalInfo().setFirstname(newName);

                User finalUser = user;
                Assertions.assertDoesNotThrow(() -> session2.refresh(finalUser));
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
                    .username("someUser3")
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
                user = session1.merge(user);
                transaction.commit();
            }


            try (var session2 = sessionFactory.openSession()) {
                var transaction = session2.beginTransaction();
                var newName = "Василий";
                user.getPersonalInfo().setFirstname(newName);

                User finalUser = user;
                Assertions.assertDoesNotThrow(() -> session2.merge(finalUser));
                transaction.commit();
                Assertions.assertNotEquals(oldName, user.getPersonalInfo().getFirstname());
                Assertions.assertEquals(newName, user.getPersonalInfo().getFirstname());
            }
        }
    }
}
