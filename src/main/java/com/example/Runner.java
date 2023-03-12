package com.example;

import com.example.config.SessionFactoryConfiguration;
import com.example.entity.Birthday;
import com.example.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
public class Runner {
    public static void main(String[] args) {
        try (var sessionFactory = SessionFactoryConfiguration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var user = User.builder()
                    .username("grayroom")
                    .firstname("Сергей")
                    .lastname("Деев")
                    .birthdate(new Birthday(LocalDate.of(1991, 2, 17)))
                    .info("""
                          {
                          "name": "Ivan",
                          "age": 25
                          }
                          """)
                    .build();
            session.persist(user);
            transaction.commit();
        }
    }
}