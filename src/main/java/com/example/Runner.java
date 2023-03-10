package com.example;

import com.example.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;

import java.time.LocalDate;

@Slf4j
public class Runner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.configure();
        try (var sessionFactory = configuration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var user = User.builder()
                    .username("grayroom")
                    .firstname("Сергей")
                    .lastname("Деев")
                    .birthDate(LocalDate.of(1991, 2, 17))
                    .age(32)
                    .build();
            session.persist(user);
            transaction.commit();
        }
    }
}