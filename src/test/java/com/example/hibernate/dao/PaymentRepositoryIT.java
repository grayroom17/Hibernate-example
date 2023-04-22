package com.example.hibernate.dao;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.Payment;
import com.example.hibernate.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

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
    void save() {
        SESSION.beginTransaction();
        var billGates = SESSION.find(User.class, 10L);
        Payment payment = Payment.builder()
                .receiver(billGates)
                .amount(100500)
                .build();
        billGates.addPayment(payment);

        PAYMENT_REPOSITORY.save(payment);
        assertNotNull(payment.getId());
        SESSION.getTransaction().commit();

        SESSION.beginTransaction();
        assertNotNull(SESSION.find(Payment.class, payment.getId()));
        assertEquals(billGates, SESSION.find(Payment.class, payment.getId()).getReceiver());
        SESSION.getTransaction().commit();
    }

    @Test
    void findById() {
        SESSION.beginTransaction();
        var payment = PAYMENT_REPOSITORY.findById(1L).orElseThrow();
        assertNotNull(payment);
        assertDoesNotThrow(payment::toString);
        SESSION.getTransaction().commit();
    }

    @Test
    void findAll() {
        SESSION.beginTransaction();
        var payment = PAYMENT_REPOSITORY.findAll();
        assertNotNull(payment);
        assertFalse(payment.isEmpty());
        SESSION.getTransaction().commit();
    }

    @Test
    void update() {
        int oldAmount;
        int newAmount;

        SESSION.beginTransaction();
        var payment = SESSION.find(Payment.class, 1L);
        oldAmount = payment.getAmount();
        newAmount = oldAmount + 500;
        payment.setAmount(newAmount);

        PAYMENT_REPOSITORY.update(payment);
        SESSION.getTransaction().commit();

        SESSION.beginTransaction();
        var updatedPayment = SESSION.find(Payment.class, 1L);
        assertNotEquals(oldAmount, updatedPayment.getAmount());
        assertEquals(newAmount, updatedPayment.getAmount());
        SESSION.getTransaction().commit();
    }

    @Test
    void deleteById() {
        SESSION.beginTransaction();
        var payment = SESSION.find(Payment.class, 2L);
        assertNotNull(payment);

        PAYMENT_REPOSITORY.delete(payment);
        SESSION.getTransaction().commit();

        SESSION.beginTransaction();
        assertNull(SESSION.find(Payment.class, 2L));
        SESSION.getTransaction().commit();
    }
}
