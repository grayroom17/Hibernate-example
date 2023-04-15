package com.example.hibernate.listeners.event_listener;

import com.example.hibernate.BaseIT;
import com.example.hibernate.config.SessionFactoryConfiguration;
import com.example.hibernate.converter.BirthdayConverter;
import com.example.hibernate.helpers.MigrationHelper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.example.hibernate.listeners.event_listener.Audit.Operation.DELETE;
import static com.example.hibernate.listeners.event_listener.Audit.Operation.INSERT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventListenerIT extends BaseIT {

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void initSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.addAttributeConverter(BirthdayConverter.class, true);
        configuration.registerTypeOverride(new JsonBinaryType(), new String[]{JsonBinaryType.INSTANCE.getName()});
        configuration.configure();

        SessionFactory factory = configuration.buildSessionFactory();
        SessionFactoryImpl sessionFactoryImpl = factory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry eventListenerRegistry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
        eventListenerRegistry.appendListeners(EventType.PRE_INSERT, new AuditTableEventListener());
        eventListenerRegistry.appendListeners(EventType.PRE_DELETE, new AuditTableEventListener());

        sessionFactory = factory;
    }

    @BeforeAll
    public static void initDbAndSessionFactory() {
        MigrationHelper.populateDb(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        sessionFactory = SessionFactoryConfiguration.buildSessionFactory();
    }

    @AfterAll
    public static void closeSessionFactory() {
        sessionFactory.close();
    }

    @Test
    void onPreInsert() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserForEventListener user = UserForEventListener.builder()
                    .username("User Name1")
                    .build();

            session.persist(user);
            session.getTransaction().commit();

            var audit = session.createQuery(
                            "select aud " +
                            "from Audit aud " +
                            "where aud.operation = :operation " +
                            "and aud.entityClass = :entityClass " +
                            "and aud.entityContent = :content",
                            Audit.class)
                    .setParameter("operation", INSERT)
                    .setParameter("entityClass", UserForEventListener.class.getSimpleName())
                    .setParameter("content", user.toString())
                    .uniqueResult();

            assertNotNull(audit);
        }
    }

    @Test
    void onPreDelete() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            UserForEventListener user = UserForEventListener.builder()
                    .username("User Name2")
                    .build();

            session.persist(user);
            session.getTransaction().commit();

            session.beginTransaction();

            var foundedUser = session.find(UserForEventListener.class, user.getId());
            assertNotNull(foundedUser);

            session.remove(foundedUser);
            session.getTransaction().commit();

            var audit = session.createQuery(
                            "select aud " +
                            "from Audit aud " +
                            "where aud.operation = :operation " +
                            "and aud.entityId = :entityId " +
                            "and aud.entityClass = :entityClass " +
                            "and aud.entityContent = :content",
                            Audit.class)
                    .setParameter("operation", DELETE)
                    .setParameter("entityId", user.getId().toString())
                    .setParameter("entityClass", UserForEventListener.class.getSimpleName())
                    .setParameter("content", user.toString())
                    .uniqueResult();

            assertNotNull(audit);
        }

    }

}
