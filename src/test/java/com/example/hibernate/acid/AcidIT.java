package com.example.hibernate.acid;

import com.example.hibernate.BaseIT;
import com.example.hibernate.acid.optimistic.lock_type.all.PaymentOptimisticLockTypeAll;
import com.example.hibernate.acid.optimistic.lock_type.dirty.PaymentOptimisticLockTypeDirty;
import com.example.hibernate.entity.Payment;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AcidIT extends BaseIT {

    @Test
    void givenEntityWithOptimisticLockVersionAndLockModeTypeOptimistic_whenTrySecondCommitWins_thenHibernateThrowOptimisticLockException() {
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
    void whenEntityWithOptimisticLockVersionAndLockModeTypeOptimistic_thenHibernateByItSelfManageEntityVersion() {
        try (var session = sessionFactory.openSession()) {

            session.beginTransaction();

            var paymentId = 1L;
            //optimistic lock mode type not necessary, because it defaults strategy
            var payment = session.find(Payment.class, paymentId, LockModeType.OPTIMISTIC);
            var oldPaymentVersion = payment.getVersion();
            payment.setAmount(payment.getAmount() * 2);

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("where id=? and version=?"));
            System.setOut(originalOut);
            outContent.reset();

            var paymentAfterUpdate = session.createQuery("select p from Payment p where p.id = :id", Payment.class)
                    .setParameter("id", paymentId)
                    .uniqueResult();

            assertNotEquals(oldPaymentVersion, paymentAfterUpdate.getVersion());
            assertTrue(paymentAfterUpdate.getVersion() > oldPaymentVersion);
        }
    }

    @Test
    void givenEntityWithOptimisticLockAllAndLockModeTypeOptimistic_whenTrySecondCommitWins_thenHibernateThrowOptimisticLockException() {
        try (var session1 = sessionFactory.openSession();
             var session2 = sessionFactory.openSession()) {

            var transaction1 = session1.beginTransaction();
            var transaction2 = session2.beginTransaction();

            var paymentId = 1L;
            var paymentTx1 = session1.find(PaymentOptimisticLockTypeAll.class, paymentId);
            paymentTx1.setAmount(paymentTx1.getAmount() * 2);

            var paymentTx2 = session2.find(PaymentOptimisticLockTypeAll.class, paymentId);
            paymentTx2.setAmount(paymentTx2.getAmount() * 4);

            transaction1.commit();
            assertThrows(OptimisticLockException.class, transaction2::commit);
        }
    }

    @Test
    void whenEntityWithOptimisticLockAllAndLockModeTypeOptimistic_thenHibernateCheckAllFieldsBeforeUpdate() {
        try (var session = sessionFactory.openSession()) {

            session.beginTransaction();

            var paymentId = 1L;
            var payment = session.find(PaymentOptimisticLockTypeAll.class, paymentId);
            payment.setAmount(payment.getAmount() * 2);

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("where id=? and amount=? and receiver_id=?"));
            System.setOut(originalOut);
            outContent.reset();
        }
    }

    @Test
    void givenEntityWithOptimisticLockDirtyAndLockModeTypeOptimistic_whenTrySecondCommitWins_thenHibernateThrowOptimisticLockException() {
        try (var session1 = sessionFactory.openSession();
             var session2 = sessionFactory.openSession()) {

            var transaction1 = session1.beginTransaction();
            var transaction2 = session2.beginTransaction();

            var paymentId = 1L;
            var paymentTx1 = session1.find(PaymentOptimisticLockTypeDirty.class, paymentId);
            paymentTx1.setAmount(paymentTx1.getAmount() * 2);

            var paymentTx2 = session2.find(PaymentOptimisticLockTypeDirty.class, paymentId);
            paymentTx2.setAmount(paymentTx2.getAmount() * 4);

            transaction1.commit();
            assertThrows(OptimisticLockException.class, transaction2::commit);
        }
    }

    @Test
    void whenEntityWithOptimisticLockDirtyAndLockModeTypeOptimistic_thenHibernateCheckOnlyUpdatedFieldsBeforeUpdate() {
        try (var session = sessionFactory.openSession()) {

            session.beginTransaction();

            var paymentId = 1L;
            var payment = session.find(PaymentOptimisticLockTypeDirty.class, paymentId);
            payment.setAmount(payment.getAmount() * 2);

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertTrue(query.contains("where id=? and amount=?"));
            assertFalse(query.contains("and receiver_id=?"));
            System.setOut(originalOut);
            outContent.reset();
        }
    }

    @NotNull
    private String prepareQuery() {
        return outContent.toString()
                .replaceAll("[\\t\\n\\r]+", " ")
                .replaceAll(" +", " ")
                .trim();
    }
}
