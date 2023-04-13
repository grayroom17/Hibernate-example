package com.example.hibernate.listeners.callback;

import com.example.hibernate.BaseIT;
import com.example.hibernate.listeners.callback.UserCallback;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HibernateCallbackIT extends BaseIT {

    @Test
    void prePersistHibernateCallback() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserCallback user = UserCallback.builder()
                    .username("user with callback1")
                    .build();

            assertNull(user.getCreatedAt());
            assertNull(user.getUpdatedAt());

            session.persist(user);
            session.flush();
            session.clear();

            var foundedUser = session.find(UserCallback.class, user.getId());
            assertEquals(user, foundedUser);
            assertNotNull(foundedUser.getCreatedAt());
            assertNull(foundedUser.getUpdatedAt());
        }
    }

    @Test
    void preUpdateHibernateCallback() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserCallback user = UserCallback.builder()
                    .username("user with callback2")
                    .build();

            session.persist(user);
            session.flush();
            session.clear();

            var foundedUser = session.find(UserCallback.class, user.getId());
            assertNull(foundedUser.getUpdatedAt());

            var newUserName = "user with callback3";
            foundedUser.setUsername(newUserName);
            var userAfterUpdate = session.merge(foundedUser);
            session.flush();
            assertNotNull(userAfterUpdate.getUpdatedAt());
        }
    }
}
