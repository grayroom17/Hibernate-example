package com.example.hibernate.one.to.one.owningside;

import com.example.hibernate.BaseIT;
import com.example.hibernate.one.to.one.ProfileForOneToOneTests;
import com.example.hibernate.one.to.one.UserForOneToOneTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class OneToOneOwningSideIT extends BaseIT {

    @Test
    void persistOwningSide_whenInverseSideNotPersisted_thenHibernateSaveOnlyOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var profile = ProfileForOneToOneTests.builder()
                    .language("RU")
                    .programmingLanguage("PHP")
                    .build();
            var user = UserForOneToOneTests.builder()
                    .username("newUser 1")
                    .build();
            user.setProfile(profile);

            session.persist(user);
            Assertions.assertDoesNotThrow(transaction::commit);
            session.clear();
            Assertions.assertEquals(user, session.find(UserForOneToOneTests.class, user.getId()));
            Assertions.assertEquals(profile, session.find(ProfileForOneToOneTests.class, profile.getId()));
        }
    }


}