package com.example.hibernate.second_level_cache;

import com.example.hibernate.BaseIT;
import com.example.hibernate.config.SessionFactoryConfiguration;
import com.example.hibernate.converter.BirthdayConverter;
import com.example.hibernate.entity.User;
import com.example.hibernate.helpers.MigrationHelper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SecondLevelCacheIT extends BaseIT {
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void initSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.addAttributeConverter(BirthdayConverter.class, true);
        configuration.registerTypeOverride(new JsonBinaryType(), new String[]{JsonBinaryType.INSTANCE.getName()});
        configuration.configure("second_level_cache/hibernate.cfg.xml");
        sessionFactory = configuration.buildSessionFactory();
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
    void test() {
        try (var session = sessionFactory.openSession()) {
        session.beginTransaction();

            var user = session.find(User.class, 10L);

            session.getTransaction().commit();
        }


    }
}
