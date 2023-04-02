package com.example.hibernate.dao;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoIT extends BaseIT {

    UserDao userDao = UserDao.getInstance();

    @Test
    void whenFindAll_thenReturnAllUsersFromDb() {
        try (var session = sessionFactory.openSession()) {
            var users = userDao.findAll(session);
            assertEquals(14, users.size());
        }
    }

    @Test
    void whenFindAllByFirstName_thenReturnAllUsersWithSpecifiedName() {
        try (var session = sessionFactory.openSession()) {
            var firstName = "Bill";
            var users = userDao.findAllByFirstName(session, firstName);
            assertEquals(1, users.size());
            assertEquals(firstName, users.stream().findFirst().orElseThrow().getPersonalInfo().getFirstname());
        }
    }

    @Test
    void whenFindLimitedUsersOrderedByFirstname_thenReturnFirstLimitedUsersOrderedByFirstname() {
        try (var session = sessionFactory.openSession()) {
            var limit = 5;
            var users = userDao.findLimitedUsersOrderedByFirstname(session, limit);
            assertEquals(limit, users.size());
        }
    }

    @Test
    void whenFindAllByCompanyName_thenReturnUsersFromSpecifiedCompany() {
        try (var session = sessionFactory.openSession()) {
            var companyName = "Google";
            var users = userDao.findAllByCompanyName(session, companyName);
            assertEquals(2, users.size());
            assertTrue(users.stream().allMatch(user -> user.getCompany().getName().equals(companyName)));
        }
    }

    @Test
    void whenFindAllPaymentsByCompanyName_thenReturnOrderedPaymentsOfUsersFromSpecifiedCompany() {
        try (var session = sessionFactory.openSession()) {
            var companyName = "Apple";
            var payments = userDao.findAllPaymentsByCompanyName(session, companyName);
            assertEquals(5, payments.size());
            assertTrue(payments.stream().allMatch(payment -> payment.getReceiver().getCompany().getName().equals(companyName)));
        }
    }

    @Test
    void whenFindAveragePaymentAmountByFirstAndLastNames_thenReturnAverageAmountOfPaymentsOfSpecifiedUser() {
        try (var session = sessionFactory.openSession()) {
            var firstName = "Steve";
            var lastName = "Jobs";
            var payments = userDao.findAveragePaymentAmountByFirstAndLastNames(session, firstName, lastName);
            assertEquals(450d, payments);
        }
    }

    @Test
    void whenFindCompanyNamesWithAvgUserPaymentsOrderedByCompanyName_thenReturnAverageAmountOfPaymentsForAllUsersOfSpecifiedCompany() {
        try (var session = sessionFactory.openSession()) {
            var entities = userDao.findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(session);

            assertEquals(3, entities.size());

            var firstEntity = entities.get(0);
            assertEquals("Apple", firstEntity[0]);
            assertEquals(410d, firstEntity[1]);

            var secondEntity = entities.get(1);
            assertEquals("Google", secondEntity[0]);
            assertEquals(400d, secondEntity[1]);

            var thirdEntity = entities.get(2);
            assertEquals("Microsoft", thirdEntity[0]);
            assertEquals(300d, thirdEntity[1]);
        }
    }

    @Test
    void whenIsItPossible_thenYesItIsPossible() {
        try (var session = sessionFactory.openSession()) {
            var entities = userDao.isItPossible(session);

            assertEquals(2, entities.size());

            var firstEntity = entities.get(0);
            assertEquals("SergeyBrin", ((User) firstEntity[0]).getUsername());
            assertEquals(500d, firstEntity[1]);

            var secondEntity = entities.get(1);
            assertEquals("SteveJobs", ((User) secondEntity[0]).getUsername());
            assertEquals(450d, secondEntity[1]);
        }
    }
}
