package com.example.hibernate.acid;

import com.example.hibernate.BaseIT;
import com.example.hibernate.entity.Payment;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AcidIT extends BaseIT {

    @Test
    void givenEntityWithOptimisticLock_whenTrySecondCommitWins_thenHibernateThrowOptimisticLockException() {
        try (var session1 = sessionFactory.openSession();
             var session2 = sessionFactory.openSession()) {

            var transaction1 = session1.beginTransaction();
            var transaction2 = session2.beginTransaction();

            var paymentId = 1L;
            //optimistic lock mode type not necessary, because it defaults strategy
            var paymentTx1 = session1.find(Payment.class, paymentId, LockModeType.OPTIMISTIC);
            paymentTx1.setAmount(paymentTx1.getAmount() * 2);

            //optimistic lock mode type not necessary, because it defaults strategy
            var paymentTx2 = session2.find(Payment.class, paymentId, LockModeType.OPTIMISTIC);
            paymentTx2.setAmount(paymentTx2.getAmount() * 4);

            transaction1.commit();
            assertThrows(OptimisticLockException.class, transaction2::commit);
        }
    }

    @Test
    void givenEntityWithOptimisticLock_whenLockModeIsOptimistic_thenHibernateByItSelfManageEntityVersion() {
        try (var session = sessionFactory.openSession()) {

            var transaction1 = session.beginTransaction();

            var paymentId = 1L;
            //optimistic lock mode type not necessary, because it defaults strategy
            var payment = session.find(Payment.class, paymentId, LockModeType.OPTIMISTIC);
            var oldPaymentVersion = payment.getVersion();
            payment.setAmount(payment.getAmount() * 2);


            var paymentAfterUpdate = session.createQuery("select p from Payment p where p.id = :id", Payment.class)
                    .setParameter("id", paymentId)
                    .uniqueResult();

            assertNotEquals(oldPaymentVersion, paymentAfterUpdate.getVersion());
            assertTrue(paymentAfterUpdate.getVersion() > oldPaymentVersion);
        }
    }
}
