package com.example.hibernate.dao;

import com.example.hibernate.entity.Payment;
import jakarta.persistence.EntityManager;

public class PaymentRepository extends BaseRepository<Long, Payment> {

    public PaymentRepository(EntityManager entityManager) {
        super(entityManager, Payment.class);
    }

}
