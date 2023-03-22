package com.example.hibernate.helpers;

import lombok.experimental.UtilityClass;
import org.flywaydb.core.Flyway;

@UtilityClass
public class MigrationHelper {
    public static void populateDb(String url, String user, String password) {
        Flyway.configure().dataSource(url, user, password)
                .load().migrate();
    }
}
