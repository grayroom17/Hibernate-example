package com.example.hibernate.envers;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.*;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
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

            @SuppressWarnings("unchecked")
            List<Object[]> revisions = auditReader.createQuery()
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

            @SuppressWarnings("unchecked")
            List<Object[]> revisions = auditReader.createQuery()
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

            @SuppressWarnings("unchecked")
            List<Object[]> revisions = auditReader.createQuery()
                    .forRevisionsOfEntity(User.class, false, true)
                    .getResultList();

            var result = revisions.stream()
                    .filter(revision -> ((User) revision[ENTITY]).getId().equals(user.getId()))
                    .filter(revision -> revision[REV_TYPE].equals(DEL))
                    .findAny().orElseThrow();

            assertNotNull(result);
        }
    }

    @Test
    void complexTest() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            User firstUser = User.builder()
                    .username("Ivan001")
                    .personalInfo(PersonalInfo.builder()
                            .birthdate(new Birthday(LocalDate.of(1991, 10, 1)))
                            .firstname("Ivan")
                            .lastname("Bunin")
                            .build())
                    .role(Role.USER)
                    .build();

            Profile profileFirstUser = Profile.builder()
                    .language("RU")
                    .programmingLanguage("Java")
                    .build();
            profileFirstUser.setUser(firstUser);

            Payment payment1FirstUser = Payment.builder()
                    .amount(100)
                    .receiver(firstUser)
                    .build();
            Payment payment2FirstUser = Payment.builder()
                    .amount(300)
                    .receiver(firstUser)
                    .build();

            User secondUser = User.builder()
                    .username("Anton556")
                    .personalInfo(PersonalInfo.builder()
                            .birthdate(new Birthday(LocalDate.of(1992, 2, 15)))
                            .firstname("Anton")
                            .lastname("Fedorov")
                            .build())
                    .role(Role.USER)
                    .build();

            Profile profileSecondUser = Profile.builder()
                    .language("EN")
                    .programmingLanguage("PHP")
                    .build();
            profileSecondUser.setUser(secondUser);

            Payment payment1SecondUser = Payment.builder()
                    .amount(500)
                    .receiver(secondUser)
                    .build();
            Payment payment2SecondUser = Payment.builder()
                    .amount(50)
                    .receiver(secondUser)
                    .build();


            Company google = Company.builder()
                    .name("Amazon")
                    .build();
            google.addUser(firstUser);
            google.addUser(secondUser);

            Team developers = Team.builder()
                    .name("developers")
                    .build();

            UserTeam userTeam1 = UserTeam.builder()
                    .joined(Instant.now())
                    .createdBy("SYSTEM")
                    .build();
            userTeam1.setTeam(developers);
            userTeam1.setUser(firstUser);
            UserTeam userTeam2 = UserTeam.builder()
                    .joined(Instant.now())
                    .createdBy("SYSTEM")
                    .build();
            userTeam2.setTeam(developers);
            userTeam2.setUser(secondUser);

            session.persist(firstUser);
            session.persist(secondUser);
            session.persist(payment1FirstUser);
            session.persist(payment2FirstUser);
            session.persist(payment1SecondUser);
            session.persist(payment2SecondUser);
            session.persist(secondUser);
            session.persist(google);
            session.persist(developers);
            session.persist(userTeam1);
            session.persist(userTeam2);

            session.getTransaction().commit();

            var auditReader = AuditReaderFactory.get(session);

            @SuppressWarnings("unchecked")
            List<Object[]> usersAud = auditReader.createQuery().forRevisionsOfEntity(User.class, false, true).getResultList();
            assertNotNull(usersAud);
            assertFalse(usersAud.isEmpty());
            var createRevFirstUser = usersAud.stream()
                    .filter(rev -> ((User) rev[ENTITY]).getId().equals(firstUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .findAny().orElseThrow();
            assertNotNull(createRevFirstUser);
            var createRevSecondUser = usersAud.stream()
                    .filter(rev -> ((User) rev[ENTITY]).getId().equals(secondUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .findAny().orElseThrow();
            assertNotNull(createRevSecondUser);

            System.out.println();

        }


    }
}
