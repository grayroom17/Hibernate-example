package com.example.hibernate.listeners.interceptor;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.Role;
import com.example.hibernate.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class InterceptorIT extends BaseIT {

    @Test
    void overrideInterceptorIT() {
        try (var session = sessionFactory.withOptions().interceptor(new GlobalInterceptor()).openSession()) {
            session.beginTransaction();

            var user = session.find(User.class, 10L);

            user.setUsername("new Name");
            user.setRole(Role.USER);

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            log.info(outContent.toString());

            assertTrue(outContent.toString().contains("There must be override interceptor logic"));
        }
    }
}
