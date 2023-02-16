package com.example;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Configuration;

@Slf4j
public class Runner {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.configure();
        try (var sessionFactory = configuration.buildSessionFactory();
             final var session = sessionFactory.openSession()) {
            log.info("Ok!");
        }
    }
}
