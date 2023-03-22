package com.example.hibernate;

import com.example.hibernate.config.SessionFactoryConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class CheckIfSessionOpenedSuccessfulIT extends BaseIT {

    @Test
    void checkIfSessionOpenedSuccessful() {
        try (var sessionFactory = SessionFactoryConfiguration.buildSessionFactory();
             var session = sessionFactory.openSession()) {
            Assertions.assertNotNull(session);
        }
    }
}