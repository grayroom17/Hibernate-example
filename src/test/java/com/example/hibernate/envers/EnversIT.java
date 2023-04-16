package com.example.hibernate.envers;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.User;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hibernate.envers.RevisionType.*;
import static org.junit.jupiter.api.Assertions.*;

class EnversIT extends BaseIT {

    private static final int ENTITY = 0;
    private static final int REV_TYPE = 2;

    @Test
    void checkCreate() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            User user = User.builder()
                    .username("new user 1")
                    .build();

            session.persist(user);

            session.getTransaction().commit();

            var auditReader = AuditReaderFactory.get(session);

            @SuppressWarnings("unchecked") List<Object[]> revisions = auditReader.createQuery()
                    .forRevisionsOfEntity(User.class, false, true)
                    .getResultList();

            var result = revisions.stream()
                    .filter(revision -> ((User) revision[ENTITY]).getId().equals(user.getId()))
                    .filter(revision -> revision[REV_TYPE].equals(ADD))
                    .findAny().orElseThrow();

            assertNotNull(result);
            assertEquals(user.getId(), ((User) result[ENTITY]).getId());
            assertEquals(user.getUsername(), ((User) result[ENTITY]).getUsername());
        }
    }

    @Test
    void checkUpdate() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            User user = User.builder()
                    .username("new user 2")
                    .build();
            session.persist(user);
            session.getTransaction().commit();

            session.beginTransaction();
            var newUserName1 = "updated user 1";
            user.setUsername(newUserName1);
            session.persist(user);
            session.getTransaction().commit();

            session.beginTransaction();
            var newUserName2 = "updated user 2";
            user.setUsername(newUserName2);
            session.persist(user);
            session.getTransaction().commit();


            var auditReader = AuditReaderFactory.get(session);

            @SuppressWarnings("unchecked") List<Object[]> revisions = auditReader.createQuery()
                    .forRevisionsOfEntity(User.class, false, true)
                    .getResultList();

            var result = revisions.stream()
                    .filter(revision -> ((User) revision[ENTITY]).getId().equals(user.getId()))
                    .filter(revision -> revision[REV_TYPE].equals(MOD))
                    .toList();

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> ((User) r[ENTITY]).getId().equals(user.getId())));
            assertEquals(newUserName1, ((User) result.stream().findFirst().orElseThrow()[ENTITY]).getUsername());
            assertEquals(newUserName2, ((User) result.stream().skip(result.size() - 1).findFirst().orElseThrow()[ENTITY]).getUsername());
        }
    }

    @Test
    void checkDelete() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            User user = User.builder()
                    .username("new user 2")
                    .build();
            session.persist(user);
            session.getTransaction().commit();

            session.beginTransaction();
            session.remove(user);
            session.getTransaction().commit();

            var auditReader = AuditReaderFactory.get(session);

            @SuppressWarnings("unchecked") List<Object[]> revisions = auditReader.createQuery()
                    .forRevisionsOfEntity(User.class, false, true)
                    .getResultList();

            var result = revisions.stream()
                    .filter(revision -> ((User) revision[ENTITY]).getId().equals(user.getId()))
                    .filter(revision -> revision[REV_TYPE].equals(DEL))
                    .findAny().orElseThrow();

            assertNotNull(result);
        }
    }
}
