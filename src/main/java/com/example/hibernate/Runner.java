package com.example.hibernate;

import com.example.hibernate.config.SessionFactoryConfiguration;
import com.example.hibernate.entity.Birthday;
import com.example.hibernate.entity.PersonalInfo;
import com.example.hibernate.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
public class Runner {
    public static void main(String[] args) {
        try (var sessionFactory = SessionFactoryConfiguration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var user = User.builder()
                    .username("fancyMonkey")
                    .personalInfo(PersonalInfo.builder()
                            .firstname("Иван")
                            .lastname("Петрушкин")
                            .birthdate(new Birthday(LocalDate.of(1995, 1, 1)))
                            .build())
                    .info("""
                          {
                          "name": "Ivan",
                          "age": 2
                          }
                          """)
                    .build();
            session.persist(user);
            transaction.commit();
        }
    }
}