package com.example.hibernate.primarykeytypes;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.Birthday;
import com.example.hibernate.entity.PersonalInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.example.hibernate.entity.Role.USER;

@Slf4j
class CheckUsersWithTableGeneratorIT extends BaseIT {

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