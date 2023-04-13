package com.example.hibernate.config;

import com.example.hibernate.converter.BirthdayConverter;
import com.example.hibernate.listeners.event_listener.AuditTableEventListener;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.experimental.UtilityClass;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;

@UtilityClass
public class SessionFactoryConfiguration {
    public static SessionFactory buildSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.addAttributeConverter(BirthdayConverter.class, true);
        configuration.registerTypeOverride(new JsonBinaryType(), new String[]{JsonBinaryType.INSTANCE.getName()});
//        configuration.setInterceptor(new GlobalInterceptor());
        configuration.configure();

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        SessionFactoryImpl sessionFactoryImpl = sessionFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry eventListenerRegistry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
        eventListenerRegistry.appendListeners(EventType.PRE_INSERT, new AuditTableEventListener());
        eventListenerRegistry.appendListeners(EventType.PRE_DELETE, new AuditTableEventListener());

        return sessionFactory;
    }
}
