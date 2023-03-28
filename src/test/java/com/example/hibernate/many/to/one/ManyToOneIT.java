package com.example.hibernate.many.to.one;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.Company;
import com.example.hibernate.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

@Slf4j
class ManyToOneIT extends BaseIT {

    @Test
    void persist_whenRelatedDataNotPersisted_thenTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForManyToOneTests.builder()
                    .name("Default CompanyForOneToManyTests 1")
                    .build();
            var user = UserForManyToOneTests.builder()
                    .username("newUser 1")
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
            var company = CompanyForManyToOneTests.builder()
                    .name("Default CompanyForOneToManyTests 2")
                    .build();
            var user = UserForManyToOneTests.builder()
                    .username("newUser 2")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();

            var foundedEntity = session.find(UserForManyToOneTests.class, user.getId());
            Assertions.assertEquals(user, foundedEntity);
            var foundedCompany = session.find(CompanyForManyToOneTests.class, foundedEntity.getCompany().getId());
            Assertions.assertEquals(company, foundedCompany);
        }
    }

    @Test
    void whenOptionalTrue_thenHibernateDoOuterLeftJoinToTableMappedByOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForManyToOneTests.builder()
                    .name("Default CompanyForOneToManyTests 3")
                    .build();
            var user = UserForManyToOneTests.builder()
                    .username("newUser 3")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(UserForManyToOneTests.class, user.getId());
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            Assertions.assertTrue(query.contains("left join company")
                                  || query.contains("left outer join company"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenOptionalFalse_thenHibernateDoInnerJoinToTableMappedByOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForManyToOneTests.builder()
                    .name("Default Company 4")
                    .build();
            var user = UserWithManyToOneWithOptionalFalse.builder()
                    .username("newUser 4")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            transaction.commit();
            session.clear();


            System.setOut(new PrintStream(outContent));
            @SuppressWarnings("unused")
            var foundedEntity = session.find(UserWithManyToOneWithOptionalFalse.class, user.getId());
            var query = outContent.toString()
                    .replaceAll("[\\t\\n\\r]+", " ")
                    .replaceAll(" +", " ")
                    .trim();
            log.warn(outContent.toString());
            Assertions.assertTrue(query.contains("join company"));
            Assertions.assertFalse(query.contains("left join company"));
            Assertions.assertFalse(query.contains("left outer join company"));
            System.setOut(originalOut);
        }
    }

    @Test
    void whenManyToOneFetchLazy_thenHibernateDoNotAnyJoinToTableMappedByOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForManyToOneTests.builder()
                    .name("Default Company 5")
                    .build();
            var user = UserWithManyToOneWithFetchLazy.builder()
                    .username("newUser 5")
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

    @Test
    void persistManyEntity_whenManyToOneCascadeTypePersis_thenHibernateSaveOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForManyToOneTests.builder()
                    .name("Default Company 6")
                    .build();
            var user = UserWithCascadeTypeAll.builder()
                    .username("newUser 6")
                    .company(company)
                    .build();

            session.persist(user);
            transaction.commit();
            session.clear();

            var foundedUser = session.find(UserWithCascadeTypeAll.class, user.getId());
            Assertions.assertEquals(user, foundedUser);
            Assertions.assertEquals(company, foundedUser.getCompany());
        }
    }

    @Test
    void persistManyEntity_whenManyToOneWithoutCascadeTypes_thenHibernateThrowsTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default Company 6")
                    .build();
            var user = User.builder()
                    .username("newUser 6")
                    .company(company)
                    .build();

            session.persist(user);
            var exception = Assertions.assertThrows(IllegalStateException.class, transaction::commit);
            Assertions.assertEquals(TransientObjectException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void mergeManyEntity_whenManyToOneCascadeTypeMerge_thenHibernateSaveOrUpdateOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForManyToOneTests.builder()
                    .name("Default CompanyForOneToManyTests 7")
                    .build();
            var defaultUserKey = 1L;
            var user = session.find(UserWithCascadeTypeAll.class, defaultUserKey);
            user.setCompany(company);

            session.merge(user);
            transaction.commit();
            session.clear();

            var foundedUser = session.find(UserWithCascadeTypeAll.class, user.getId());
            Assertions.assertEquals(user, foundedUser);
            Assertions.assertNotNull(foundedUser.getCompany());
            Assertions.assertNotNull(foundedUser.getCompany().getId());
            Assertions.assertEquals(company.getName(), foundedUser.getCompany().getName());
        }
    }

    @Test
    void mergeManyEntity_whenManyToOneWithoutCascadeTypes_thenHibernateThrowsTransientObjectException() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = Company.builder()
                    .name("Default CompanyForOneToManyTests 7")
                    .build();
            var defaultUserKey = 1L;
            var user = session.find(User.class, defaultUserKey);
            user.setCompany(company);

            session.merge(user);
            var exception = Assertions.assertThrows(IllegalStateException.class, transaction::commit);
            Assertions.assertEquals(TransientObjectException.class, exception.getCause().getClass());
            transaction.rollback();
        }
    }

    @Test
    void removeManyEntity_whenManyToOneCascadeTypeRemove_thenHibernateRemoveManyEntityBeforeOne() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var company = CompanyForManyToOneTests.builder()
                    .name("Default CompanyForOneToManyTests 8")
                    .build();
            var user = UserWithCascadeTypeAll.builder()
                    .username("newUser 8")
                    .company(company)
                    .build();

            session.persist(user);
            transaction.commit();
            session.clear();

            session.beginTransaction();
            Assertions.assertEquals(user, session.find(UserWithCascadeTypeAll.class, user.getId()));
            session.clear();

            session.remove(user);
            session.getTransaction().commit();

            Assertions.assertNull(session.find(UserWithCascadeTypeAll.class, user.getId()));
        }
    }

    @Test
    void removeManyEntity_whenManyToOneWithoutCascadeTypes_thenHibernateDeleteOnlyManyEntity() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var company = Company.builder()
                    .name("Default CompanyForOneToManyTests 8")
                    .build();
            var user = User.builder()
                    .username("newUser 8")
                    .company(company)
                    .build();

            session.persist(company);
            session.persist(user);
            session.getTransaction().commit();
            session.clear();

            var transaction = session.beginTransaction();
            session.remove(user);
            Assertions.assertDoesNotThrow(transaction::commit);
            var foundedCompany = session.find(Company.class, company.getId());
            Assertions.assertNotNull(foundedCompany);
            Assertions.assertEquals(company, foundedCompany);
        }
    }

    @Test
    void refreshManyEntity_whenManyToOneCascadeTypeRefresh_thenHibernateRefreshOneEntityBeforeMany() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var userWithCompany = session.find(UserWithCascadeTypeAll.class, 4L);
            var company = userWithCompany.getCompany();

            var newUsername = "new Username";
            var oldUsername = userWithCompany.getUsername();
            Assertions.assertNotEquals(newUsername, oldUsername);
            userWithCompany.setUsername(newUsername);

            var companyName = company.getName();
            company.setName(companyName + "Changed");


            session.refresh(userWithCompany);
            transaction.commit();
            session.clear();

            Assertions.assertEquals(oldUsername, userWithCompany.getUsername());
            Assertions.assertEquals(companyName, userWithCompany.getCompany().getName());
        }
    }

    @Test
    void refreshManyEntity_whenManyToOneWithoutCascadeTypes_thenHibernateRefreshOnlyManyEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var userWithCompany = session.find(User.class, 4L);
            var company = userWithCompany.getCompany();

            var newUsername = "new Username";
            var oldUsername = userWithCompany.getUsername();
            Assertions.assertNotEquals(newUsername, oldUsername);
            userWithCompany.setUsername(newUsername);

            var newCompanyName = "new Company name";
            var oldCompanyName = company.getName();
            company.setName(newCompanyName);


            session.refresh(userWithCompany);
            transaction.commit();

            Assertions.assertEquals(oldUsername, userWithCompany.getUsername());
            Assertions.assertNotEquals(oldCompanyName, userWithCompany.getCompany().getName());
            Assertions.assertEquals(newCompanyName, userWithCompany.getCompany().getName());
        }
    }

    @Test
    void detachManyEntity_whenManyToOneCascadeTypeDetach_thenHibernateDetachManyAndOneEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var userWithCompany = session.find(UserWithCascadeTypeAll.class, 4L);
            var company = userWithCompany.getCompany();

            Assertions.assertTrue(session.contains(userWithCompany));
            Assertions.assertTrue(session.contains(company));

            session.detach(userWithCompany);
            transaction.commit();

            Assertions.assertFalse(session.contains(userWithCompany));
            Assertions.assertFalse(session.contains(company));
        }
    }

    @Test
    void detachManyEntity_whenManyToOneWithoutCascadeTypes_thenHibernateDetachOnlyManyEntity() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();

            var userWithCompany = session.find(User.class, 4L);
            var company = userWithCompany.getCompany();

            Assertions.assertTrue(session.contains(userWithCompany));
            Assertions.assertTrue(session.contains(company));

            session.detach(userWithCompany);
            transaction.commit();

            Assertions.assertFalse(session.contains(userWithCompany));
            Assertions.assertTrue(session.contains(company));
        }
    }

}