package com.example.hibernate.ordering;

import com.example.hibernate.BaseIT;
import com.example.hibernate.ordering.in_db.order_by.CompanyWithOrderBy;
import com.example.hibernate.ordering.in_db.order_by.UserForOrderBy;
import com.example.hibernate.ordering.in_memory.CompanyWithSortedCollection;
import com.example.hibernate.ordering.in_memory.UserForSortedCollection;
import com.example.hibernate.ordering.in_memory.map.CompanyWithMap;
import com.example.hibernate.ordering.in_memory.map.UserForMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
class OrderingIT extends BaseIT {

    @Test
    void orderingWithSql() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var company = CompanyWithOrderBy.builder()
                    .name("Another one Company")
                    .build();

            var user1 = UserForOrderBy.builder()
                    .username("adca")
                    .company(company)
                    .build();
            var user2 = UserForOrderBy.builder()
                    .username("asdf")
                    .company(company)
                    .build();
            var user3 = UserForOrderBy.builder()
                    .username("sdfg")
                    .company(company)
                    .build();
            var user4 = UserForOrderBy.builder()
                    .username("bsbsf")
                    .company(company)
                    .build();
            var user5 = UserForOrderBy.builder()
                    .username("dfgfhghgf")
                    .company(company)
                    .build();
            var user6 = UserForOrderBy.builder()
                    .username("abc")
                    .company(company)
                    .build();
            var user7 = UserForOrderBy.builder()
                    .username("aaa")
                    .company(company)
                    .build();


            company.getUsers().addAll(Set.of(user1, user2, user3, user4, user5, user6, user7));

            session.persist(company);
            session.getTransaction().commit();
            session.clear();

            var foundedCompany = session.find(CompanyWithOrderBy.class, company.getId());
            var orderedUsername = foundedCompany.getUsers().stream().map(UserForOrderBy::getUsername).toList();
            var usernames = company.getUsers().stream().map(UserForOrderBy::getUsername).toList();
            assertNotEquals(usernames, orderedUsername);
            var sortedUsernames = usernames.stream().sorted().toList();
            assertEquals(sortedUsernames, orderedUsername);
            log.info("Unsorted: {}", usernames);
            log.info("Ordered: {}", orderedUsername);
            log.info("Sorted: {}", sortedUsernames);
        }
    }

    @Test
    void inMemoryCollectionSort() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var company = CompanyWithSortedCollection.builder()
                    .name("One else company")
                    .build();

            var user1 = UserForSortedCollection.builder()
                    .username("asgdca")
                    .company(company)
                    .build();
            var user2 = UserForSortedCollection.builder()
                    .username("adkf")
                    .company(company)
                    .build();
            var user3 = UserForSortedCollection.builder()
                    .username("aajsfl")
                    .company(company)
                    .build();
            var user4 = UserForSortedCollection.builder()
                    .username("paojfds")
                    .company(company)
                    .build();
            var user5 = UserForSortedCollection.builder()
                    .username("asdkfnn")
                    .company(company)
                    .build();
            var user6 = UserForSortedCollection.builder()
                    .username("aasdfnasld")
                    .company(company)
                    .build();
            var user7 = UserForSortedCollection.builder()
                    .username("adfjalsndfa")
                    .company(company)
                    .build();


            company.getUsers().addAll(Set.of(user1, user2, user3, user4, user5, user6, user7));

            session.persist(company);
            session.getTransaction().commit();
            session.clear();

            var foundedCompany = session.find(CompanyWithSortedCollection.class, company.getId());
            var orderedUsername = foundedCompany.getUsers().stream().map(UserForSortedCollection::getUsername).toList();
            var sortedUsernames = company.getUsers().stream().map(UserForSortedCollection::getUsername).toList();
            assertEquals(sortedUsernames, orderedUsername);
            log.info("Ordered: {}", orderedUsername);
            log.info("Sorted: {}", sortedUsernames);
        }
    }

    @Test
    void inMemoryMapSort() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var company = CompanyWithMap.builder()
                    .name("Just Company")
                    .build();

            var user1 = UserForMap.builder()
                    .username("adfds")
                    .company(company)
                    .build();
            var user2 = UserForMap.builder()
                    .username("basg")
                    .company(company)
                    .build();
            var user3 = UserForMap.builder()
                    .username("dagdgas")
                    .company(company)
                    .build();
            var user4 = UserForMap.builder()
                    .username("gadads")
                    .company(company)
                    .build();
            var user5 = UserForMap.builder()
                    .username("adadfads")
                    .company(company)
                    .build();
            var user6 = UserForMap.builder()
                    .username("adfasdf")
                    .company(company)
                    .build();
            var user7 = UserForMap.builder()
                    .username("afvasdfv")
                    .company(company)
                    .build();


            company.addUser(user1);
            company.addUser(user2);
            company.addUser(user3);
            company.addUser(user4);
            company.addUser(user5);
            company.addUser(user6);
            company.addUser(user7);

            session.persist(company);
            session.getTransaction().commit();
            session.clear();

            var foundedCompany = session.find(CompanyWithMap.class, company.getId());
            var orderedUsername = foundedCompany.getUsers().values().stream().map(UserForMap::getUsername).toList();
            var sortedUsernames = company.getUsers().values().stream().map(UserForMap::getUsername).toList();
            assertEquals(sortedUsernames, orderedUsername);
            log.info("Ordered: {}", orderedUsername);
            log.info("Sorted: {}", sortedUsernames);
        }
    }
}
