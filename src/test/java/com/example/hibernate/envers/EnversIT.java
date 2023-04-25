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
                    .role(Role.USER)
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
                    .role(Role.USER)
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
                    .role(Role.USER)
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
                    .build();
            Payment payment2FirstUser = Payment.builder()
                    .amount(300)
                    .build();
            firstUser.addPayment(payment1FirstUser);
            firstUser.addPayment(payment2FirstUser);

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
                    .build();
            Payment payment2SecondUser = Payment.builder()
                    .amount(50)
                    .build();
            secondUser.addPayment(payment1SecondUser);
            secondUser.addPayment(payment2SecondUser);


            Company amazon = Company.builder()
                    .name("Amazon")
                    .build();
            amazon.addUser(firstUser);
            amazon.addUser(secondUser);

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
            session.persist(amazon);
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

            @SuppressWarnings("unchecked")
            List<Object[]> profileAud = auditReader.createQuery().forRevisionsOfEntity(Profile.class, false, true).getResultList();
            assertNotNull(profileAud);
            assertFalse(profileAud.isEmpty());
            var addRevFirstUserProfile = profileAud.stream()
                    .filter(rev -> ((Profile) rev[ENTITY]).getId().equals(profileFirstUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .findAny().orElseThrow();
            assertNotNull(addRevFirstUserProfile);
            var addRevSecondUserProfile = profileAud.stream()
                    .filter(rev -> ((Profile) rev[ENTITY]).getId().equals(profileSecondUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .findAny().orElseThrow();
            assertNotNull(addRevSecondUserProfile);

            @SuppressWarnings("unchecked")
            List<Object[]> paymentsAud = auditReader.createQuery().forRevisionsOfEntity(Payment.class, false, true).getResultList();
            assertNotNull(paymentsAud);
            assertFalse(paymentsAud.isEmpty());
            var createRevOfFirstUserPayments = paymentsAud.stream()
                    .filter(rev -> ((Payment) rev[ENTITY]).getReceiver().equals(firstUser))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .toList();
            assertNotNull(createRevOfFirstUserPayments);
            assertFalse(createRevOfFirstUserPayments.isEmpty());
            var createRevOfSecondUserPayments = paymentsAud.stream()
                    .filter(rev -> ((Payment) rev[ENTITY]).getReceiver().equals(secondUser))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .toList();
            assertNotNull(createRevOfSecondUserPayments);
            assertFalse(createRevOfSecondUserPayments.isEmpty());

            @SuppressWarnings("unchecked")
            List<Object[]> companyAud = auditReader.createQuery().forRevisionsOfEntity(Company.class, false, true).getResultList();
            assertNotNull(companyAud);
            assertFalse(companyAud.isEmpty());
            var amazonAddRev = companyAud.stream()
                    .filter(rev -> ((Company) rev[ENTITY]).getId().equals(amazon.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .findAny().orElseThrow();
            assertNotNull(amazonAddRev);

            @SuppressWarnings("unchecked")
            List<Object[]> userTeamAud = auditReader.createQuery().forRevisionsOfEntity(UserTeam.class, false, true).getResultList();
            assertNotNull(userTeamAud);
            assertFalse(userTeamAud.isEmpty());
            var userTeamAddRevs = userTeamAud.stream()
                    .filter(rev -> ((UserTeam) rev[ENTITY]).getUser().getId().equals(firstUser.getId())
                                   || ((UserTeam) rev[ENTITY]).getUser().getId().equals(secondUser.getId()))
                    .filter(rev -> ((UserTeam) rev[ENTITY]).getTeam().getId().equals(developers.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .toList();
            assertNotNull(userTeamAddRevs);
            assertFalse(userTeamAddRevs.isEmpty());
            assertEquals(2, userTeamAddRevs.size());

            @SuppressWarnings("unchecked")
            List<Object[]> teamAud = auditReader.createQuery().forRevisionsOfEntity(Team.class, false, true).getResultList();
            assertNotNull(teamAud);
            assertFalse(teamAud.isEmpty());
            var teamAddRev = teamAud.stream()
                    .filter(rev -> ((Team) rev[ENTITY]).getId().equals(developers.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(ADD))
                    .toList();
            assertNotNull(teamAddRev);
            assertFalse(teamAddRev.isEmpty());
            assertEquals(1, teamAddRev.size());

            session.beginTransaction();

            session.remove(profileSecondUser);
            session.remove(userTeam2);
            secondUser.getPayments().forEach(session::remove);
            session.remove(secondUser);

            firstUser.setUsername("Pupkin007");
            payment1FirstUser.setAmount(1000);
            payment2FirstUser.setAmount(500);
            session.remove(payment2FirstUser);
            profileFirstUser.setLanguage("EN");

            amazon.setName("Amazon LLC");

            developers.setName("devs team");

            session.persist(firstUser);
            session.persist(payment1FirstUser);
            session.persist(payment2FirstUser);
            session.persist(profileFirstUser);
            session.persist(amazon);
            session.persist(developers);

            session.getTransaction().commit();

            //noinspection unchecked
            usersAud = auditReader.createQuery().forRevisionsOfEntity(User.class, false, true).getResultList();
            assertNotNull(usersAud);
            assertFalse(usersAud.isEmpty());
            var modRevFirstUser = usersAud.stream()
                    .filter(rev -> ((User) rev[ENTITY]).getId().equals(firstUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(MOD))
                    .findAny().orElseThrow();
            assertNotNull(modRevFirstUser);
            var delRevSecondUser = usersAud.stream()
                    .filter(rev -> ((User) rev[ENTITY]).getId().equals(secondUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(DEL))
                    .findAny().orElseThrow();
            assertNotNull(delRevSecondUser);

            //noinspection unchecked
            profileAud = auditReader.createQuery().forRevisionsOfEntity(Profile.class, false, true).getResultList();
            assertNotNull(profileAud);
            assertFalse(profileAud.isEmpty());
            var modRevFirstUserProfile = profileAud.stream()
                    .filter(rev -> ((Profile) rev[ENTITY]).getId().equals(profileFirstUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(MOD))
                    .findAny().orElseThrow();
            assertNotNull(modRevFirstUserProfile);
            var delRevSecondUserProfile = profileAud.stream()
                    .filter(rev -> ((Profile) rev[ENTITY]).getId().equals(profileSecondUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(DEL))
                    .findAny().orElseThrow();
            assertNotNull(delRevSecondUserProfile);

            //noinspection unchecked
            paymentsAud = auditReader.createQuery().forRevisionsOfEntity(Payment.class, false, true).getResultList();
            assertNotNull(paymentsAud);
            assertFalse(paymentsAud.isEmpty());
            var modRevOfFirstUserPayments = paymentsAud.stream()
                    .filter(rev -> ((Payment) rev[ENTITY]).getId().equals(payment1FirstUser.getId())
                                   || ((Payment) rev[ENTITY]).getId().equals(payment2FirstUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(MOD))
                    .toList();
            assertNotNull(modRevOfFirstUserPayments);
            assertFalse(modRevOfFirstUserPayments.isEmpty());
            assertEquals(2, modRevOfFirstUserPayments.size());
            var delRevOfSecondUserPayments = paymentsAud.stream()
                    .filter(rev -> ((Payment) rev[ENTITY]).getId().equals(payment1SecondUser.getId())
                                   || ((Payment) rev[ENTITY]).getId().equals(payment2SecondUser.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(DEL))
                    .toList();
            assertNotNull(delRevOfSecondUserPayments);
            assertFalse(delRevOfSecondUserPayments.isEmpty());
            assertEquals(2, delRevOfSecondUserPayments.size());

            //noinspection unchecked
            companyAud = auditReader.createQuery().forRevisionsOfEntity(Company.class, false, true).getResultList();
            assertNotNull(companyAud);
            assertFalse(companyAud.isEmpty());
            var amazonModRev = companyAud.stream()
                    .filter(rev -> ((Company) rev[ENTITY]).getId().equals(amazon.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(MOD))
                    .findAny().orElseThrow();
            assertNotNull(amazonModRev);

            //noinspection unchecked
            userTeamAud = auditReader.createQuery().forRevisionsOfEntity(UserTeam.class, false, true).getResultList();
            assertNotNull(userTeamAud);
            assertFalse(userTeamAud.isEmpty());
            var userTeamDelRevs = userTeamAud.stream()
                    .filter(rev -> ((UserTeam) rev[ENTITY]).getId().equals(userTeam2.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(DEL))
                    .toList();
            assertNotNull(userTeamDelRevs);
            assertFalse(userTeamDelRevs.isEmpty());
            assertEquals(1, userTeamDelRevs.size());

            //noinspection unchecked
            teamAud = auditReader.createQuery().forRevisionsOfEntity(Team.class, false, true).getResultList();
            assertNotNull(teamAud);
            assertFalse(teamAud.isEmpty());
            var teamModRev = teamAud.stream()
                    .filter(rev -> ((Team) rev[ENTITY]).getId().equals(developers.getId()))
                    .filter(rev -> rev[REV_TYPE].equals(MOD))
                    .toList();
            assertNotNull(teamModRev);
            assertFalse(teamModRev.isEmpty());
            assertEquals(1, teamModRev.size());

        }
    }
}
