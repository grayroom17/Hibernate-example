package com.example.hibernate.dao;

import com.example.hibernate.BaseIT;
import com.example.hibernate.dto.PaymentFilter;
import com.example.hibernate.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryDslDaoIT extends BaseIT {

    QueryDslDao dao = QueryDslDao.getInstance();

    @Test
    void whenFindAll_thenReturnAllUsersFromDb() {
        try (var session = sessionFactory.openSession()) {
            var users = dao.findAll(session);
            assertEquals(14, users.size());
        }
    }

    @Test
    void whenFindAllByFirstName_thenReturnAllUsersWithSpecifiedName() {
        try (var session = sessionFactory.openSession()) {
            var firstName = "Bill";
            var users = dao.findAllByFirstName(session, firstName);
            assertEquals(1, users.size());
            assertEquals(firstName, users.stream().findFirst().orElseThrow().getPersonalInfo().getFirstname());
        }
    }

    @Test
    void whenFindLimitedUsersOrderedByFirstname_thenReturnFirstLimitedUsersOrderedByFirstname() {
        try (var session = sessionFactory.openSession()) {
            var limit = 5;
            var users = dao.findLimitedUsersOrderedByFirstname(session, limit);
            assertEquals(limit, users.size());
        }
    }

    @Test
    void whenFindAllByCompanyName_thenReturnUsersFromSpecifiedCompany() {
        try (var session = sessionFactory.openSession()) {
            var companyName = "Google";
            var users = dao.findAllByCompanyName(session, companyName);
            assertEquals(2, users.size());
            assertTrue(users.stream().allMatch(user -> user.getCompany().getName().equals(companyName)));
        }
    }

    @Test
    void whenFindAllPaymentsByCompanyName_thenReturnOrderedPaymentsOfUsersFromSpecifiedCompany() {
        try (var session = sessionFactory.openSession()) {
            var companyName = "Apple";
            var payments = dao.findAllPaymentsByCompanyName(session, companyName);
            assertEquals(5, payments.size());
            assertTrue(payments.stream().allMatch(payment -> payment.getReceiver().getCompany().getName().equals(companyName)));
        }
    }

    @Test
    void whenFindAveragePaymentAmountByFirstAndLastNames_thenReturnAverageAmountOfPaymentsOfSpecifiedUser() {
        try (var session = sessionFactory.openSession()) {
            var firstName = "Steve";
            var lastName = "Jobs";
            var payments = dao.findAveragePaymentAmountByFirstAndLastNames(session, firstName, lastName);
            assertEquals(450d, payments);
        }
    }

    @Test
    void whenFindPaymentsAmountByFilter_thenReturnAverageAmountOfPaymentsOfSpecifiedUser() {
        try (var session = sessionFactory.openSession()) {
            var firstName = "Steve";
            var lastName = "Jobs";
            var filter = PaymentFilter.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();
            var payments = dao.findPaymentsAmountByFilter(session, filter);
            assertEquals(450d, payments);
        }
    }

    @Test
    void whenFindCompanyNamesWithAvgUserPaymentsOrderedByCompanyName_thenReturnAverageAmountOfPaymentsForAllUsersOfSpecifiedCompany() {
        try (var session = sessionFactory.openSession()) {
            var tuples = dao.findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(session);

            assertEquals(3, tuples.size());

            var firstTuple = tuples.get(0);
            assertEquals("Apple", firstTuple.get(0, String.class));
            assertEquals(410d, firstTuple.get(1, Double.class));

            var secondTuple = tuples.get(1);
            assertEquals("Google", secondTuple.get(0, String.class));
            assertEquals(400d, secondTuple.get(1, Double.class));

            var thirdTuple = tuples.get(2);
            assertEquals("Microsoft", thirdTuple.get(0, String.class));
            assertEquals(300d, thirdTuple.get(1, Double.class));
        }
    }

    @Test
    void whenIsItPossible_thenYesItIsPossible() {
        try (var session = sessionFactory.openSession()) {
            var tuples = dao.isItPossible(session);

            assertEquals(2, tuples.size());

            var firstTuple = tuples.get(0);
            assertEquals("SergeyBrin", Objects.requireNonNull(firstTuple.get(0, User.class)).getUsername());
            assertEquals(500d, firstTuple.get(1, Double.class));

            var secondTuple = tuples.get(1);
            assertEquals("SteveJobs", Objects.requireNonNull(secondTuple.get(0, User.class)).getUsername());
            assertEquals(450d, secondTuple.get(1, Double.class));
        }
    }
}
