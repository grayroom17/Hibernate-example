package com.example.hibernate.primarykeytypes;

import com.example.hibernate.BaseIT;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
class CheckProfileWithPrimaryKeyAsForeignKeyIT extends BaseIT {

    @Test
    void persist() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var profile = ProfileWithPrimaryKeyAsForeignKey.builder()
                    .language("EN")
                    .programmingLanguage("Java 17")
                    .build();
            var user = UserForPrimaryKeyAsForeignKeyCheck.builder()
                    .username("notSavedUser")
                    .build();

            session.persist(user);
            profile.setUser(user);
            session.getTransaction().commit();
            session.clear();


            var foundedUser = session.find(UserForPrimaryKeyAsForeignKeyCheck.class, user.getId());
            Assertions.assertEquals(user, foundedUser);
            Assertions.assertNotNull(foundedUser.getProfile().getId());
            Assertions.assertEquals(foundedUser.getId(), foundedUser.getProfile().getId());
        }
    }
}