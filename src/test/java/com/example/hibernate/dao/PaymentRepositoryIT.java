package com.example.hibernate.dao;

import com.example.hibernate.BaseIT;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentRepositoryIT extends BaseIT {
    private final static PaymentRepository PAYMENT_REPOSITORY;
    private final static Session SESSION;

    static {
        SESSION = (Session) Proxy.newProxyInstance(SessionFactory.class.getClassLoader(),
                new Class[]{Session.class},
                (proxy, method, args) -> method.invoke(sessionFactory.getCurrentSession(), args));

        PAYMENT_REPOSITORY = new PaymentRepository(SESSION);
    }

    @Test
    void findById() {
        SESSION.beginTransaction();
        var payment = PAYMENT_REPOSITORY.findById(1L).orElseThrow();
        assertNotNull(payment);
        assertDoesNotThrow(payment::toString);
        SESSION.getTransaction().commit();
    }
}
