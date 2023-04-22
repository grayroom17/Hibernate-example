package com.example.hibernate.dao;

import com.example.hibernate.BaseIT;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentRepositoryIT extends BaseIT {
    private final static PaymentRepository PAYMENT_REPOSITORY;

    static {
        PAYMENT_REPOSITORY = new PaymentRepository(sessionFactory);
    }

    @Test
    void findById() {
        var payment = PAYMENT_REPOSITORY.findById(1L).orElseThrow();
        assertNotNull(payment);
        //noinspection ResultOfMethodCallIgnored
        assertThrows(LazyInitializationException.class, payment::toString);
    }
}
