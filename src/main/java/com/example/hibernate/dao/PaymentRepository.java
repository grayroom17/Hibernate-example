package com.example.hibernate.dao;

import com.example.hibernate.entity.Payment;
import org.hibernate.SessionFactory;

public class PaymentRepository extends BaseRepository<Long, Payment> {

    public PaymentRepository(SessionFactory sessionFactory) {
        super(sessionFactory, Payment.class);
    }

}
