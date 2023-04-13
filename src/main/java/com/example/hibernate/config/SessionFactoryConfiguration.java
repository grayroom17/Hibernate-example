package com.example.hibernate.config;

import com.example.hibernate.converter.BirthdayConverter;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.experimental.UtilityClass;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;

@UtilityClass
public class SessionFactoryConfiguration {
    public static SessionFactory buildSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        configuration.addAttributeConverter(BirthdayConverter.class, true);
        configuration.registerTypeOverride(new JsonBinaryType(), new String[]{JsonBinaryType.INSTANCE.getName()});
//        configuration.setInterceptor(new GlobalInterceptor());
        configuration.configure();

        return configuration.buildSessionFactory();
    }
}
