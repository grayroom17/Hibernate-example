package com.example.hibernate.acid;

import com.example.hibernate.BaseIT;
import com.example.hibernate.acid.optimistic.lock_type.all.PaymentOptimisticLockTypeAll;
import com.example.hibernate.acid.optimistic.lock_type.dirty.PaymentOptimisticLockTypeDirty;
import com.example.hibernate.acid.pessimistic.PaymentPessimisticLock;
import com.example.hibernate.entity.Payment;
import com.example.hibernate.entity.User;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AcidIT extends BaseIT {

    @Test
    void givenEntityWithOptimisticLockVersionAndLockModeTypeOptimistic_whenTryLastCommitWins_thenHibernateThrowOptimisticLockException() {
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
    void givenEntityWithOptimisticLockAllAndLockModeTypeOptimistic_whenTryLastCommitWins_thenHibernateThrowOptimisticLockException() {
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
    void givenEntityWithOptimisticLockDirtyAndLockModeTypeOptimistic_whenTryLastCommitWins_thenHibernateThrowOptimisticLockException() {
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

    //lock timeout do not work???
    @Test
    void givenEntity_whenTryLastCommitWinsWithPessimisticLock_thenHibernateThrowLockTimeoutException() {
        try (var session1 = sessionFactory.openSession();
             var session2 = sessionFactory.openSession()) {

            session1.beginTransaction();
            session2.beginTransaction();

            var paymentId = 1L;
            var paymentTx1 = session1.createQuery("select p from PaymentPessimisticLock p where p.id = :id", PaymentPessimisticLock.class)
                    .setParameter("id", paymentId)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
//                    .setHint(AvailableSettings.JAKARTA_LOCK_TIMEOUT, 5000)
                    .setHint(AvailableSettings.JAKARTA_LOCK_TIMEOUT, 0)
                    .uniqueResult();
            paymentTx1.setAmount(paymentTx1.getAmount() * 2);

//            Map<String, Object> properties = Map.of(AvailableSettings.JAKARTA_LOCK_TIMEOUT, 5000);
            Map<String, Object> properties = Map.of(AvailableSettings.JAKARTA_LOCK_TIMEOUT, 0);
            assertThrows(LockTimeoutException.class, () ->
                    session2.find(PaymentPessimisticLock.class, paymentId, LockModeType.PESSIMISTIC_WRITE, properties));
//            var paymentTx2 = session2.find(PaymentPessimisticLock.class, paymentId,LockModeType.PESSIMISTIC_WRITE, properties);
//            paymentTx2.setAmount(paymentTx2.getAmount() * 4);
//
//            session2.getTransaction().commit();
//            session1.getTransaction().commit();
        }
    }

    @Test
    void givenEntityAndDefaultReadOnlyTrue_whenTryToUpdateEntity_thenHibernateDoNotUpdateAllEntity() {
        try (var session = sessionFactory.openSession()) {

            session.beginTransaction();
            session.setDefaultReadOnly(true);

            var userId = 10L;
            var user = session.find(User.class, userId);
            user.setUsername("newUserName");

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertFalse(query.contains("update users"));
            System.setOut(originalOut);
            outContent.reset();
        }
    }

    @Test
    void givenEntityAndReadOnlyTrue_whenTryToUpdateEntity_thenHibernateDoNotUpdateSpecifiedEntity() {
        try (var session = sessionFactory.openSession()) {

            session.beginTransaction();

            var userId = 10L;
            var user = session.find(User.class, userId);
            session.setReadOnly(user, true);

            user.setUsername("newUserName");

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertFalse(query.contains("update users"));
            System.setOut(originalOut);
            outContent.reset();
        }
    }

    @Test
    void givenEntity_whenFindEntityWithReadOnlyTrue_thenHibernateDoNotUpdateSpecifiedEntities() {
        try (var session = sessionFactory.openSession()) {

            session.beginTransaction();

            var userId = 10L;
            var user = session.createQuery("select u from User u where u.id = :id", User.class)
                    .setParameter("id", userId)
                    .setReadOnly(true)
                    .uniqueResult();

            user.setUsername("newUserName");

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            log.info(outContent.toString());
            var query = prepareQuery();
            assertFalse(query.contains("update users"));
            System.setOut(originalOut);
            outContent.reset();
        }
    }

    @Test
    void givenEntity_whenSetTransactionReadOnlyModeInDataBase_thenHibernateThrow() {
        try (var session = sessionFactory.openSession()) {

            session.beginTransaction();
            //noinspection deprecation
            session.createNativeQuery("set transaction read only").executeUpdate();

            var userId = 10L;
            var user = session.find(User.class, userId);
            user.setUsername("newUserName");

            var transaction = session.getTransaction();
            var exception = assertThrows(PersistenceException.class, transaction::commit);
            assertEquals("ERROR: cannot execute UPDATE in a read-only transaction", exception.getCause().getCause().getMessage());
            session.getTransaction().rollback();
        }
    }

    @Test
    void givenData_whenTryFindDataWithNonTransactionalDataAccess_thenHibernateFindSpecifiedData() {
        try (var session = sessionFactory.openSession()) {
            var users = session.createQuery("select u from User u", User.class).list();
            assertNotNull(users);
            assertFalse(users.isEmpty());
        }
    }

    @Test
    void givenData_whenTryFindDataAndUpdateItWithNonTransactionalDataAccess_thenHibernateUpdateNothing() {
        try (var session = sessionFactory.openSession()) {
            var users = session.createQuery("select u from User u", User.class).list();

            assertNotNull(users);
            assertFalse(users.isEmpty());

            var someUser = users.stream().findFirst().orElseThrow();
            someUser.setUsername("newUserName");

            System.setOut(new PrintStream(outContent));
            session.merge(someUser);
            log.info(outContent.toString());
            var query = prepareQuery();

            assertFalse(query.contains("update users"));

            System.setOut(originalOut);
            outContent.reset();
        }
    }

    @Test
    void givenData_whenTryFindDataAndUpdateItThroughFlushWithNonTransactionalDataAccessAnd_thenHibernateThrows() {
        try (var session = sessionFactory.openSession()) {
            var users = session.createQuery("select u from User u", User.class).list();

            assertNotNull(users);
            assertFalse(users.isEmpty());

            var someUser = users.stream().findFirst().orElseThrow();
            someUser.setUsername("newUserName");

            session.merge(someUser);
            var exception = assertThrows(TransactionRequiredException.class, session::flush);
            assertEquals("no transaction is in progress", exception.getMessage());
        }
    }

}
