package com.example.hibernate.listeners.event_listener;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.User;
import org.junit.jupiter.api.Test;

import static com.example.hibernate.listeners.event_listener.Audit.Operation.DELETE;
import static com.example.hibernate.listeners.event_listener.Audit.Operation.INSERT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventListenerIT extends BaseIT {

    @Test
    void onPreInsert() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            User user = User.builder()
                    .username("User Name1")
                    .build();

            session.persist(user);
            session.getTransaction().commit();

            var audit = session.createQuery(
                            "select aud " +
                            "from Audit aud " +
                            "where aud.operation = :operation " +
                            "and aud.entityClass = :entityClass " +
                            "and aud.entityContent = :content",
                            Audit.class)
                    .setParameter("operation", INSERT)
                    .setParameter("entityClass", User.class.getSimpleName())
                    .setParameter("content", user.toString())
                    .uniqueResult();

            assertNotNull(audit);
        }
    }

    @Test
    void onPreDelete() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            User user = User.builder()
                    .username("User Name2")
                    .build();

            session.persist(user);
            session.getTransaction().commit();

            session.beginTransaction();

            var foundedUser = session.find(User.class, user.getId());
            assertNotNull(foundedUser);

            session.remove(foundedUser);
            session.getTransaction().commit();

            var audit = session.createQuery(
                            "select aud " +
                            "from Audit aud " +
                            "where aud.operation = :operation " +
                            "and aud.entityId = :entityId " +
                            "and aud.entityClass = :entityClass " +
                            "and aud.entityContent = :content",
                            Audit.class)
                    .setParameter("operation", DELETE)
                    .setParameter("entityId", user.getId().toString())
                    .setParameter("entityClass", User.class.getSimpleName())
                    .setParameter("content", user.toString())
                    .uniqueResult();

            assertNotNull(audit);
        }

    }

}
