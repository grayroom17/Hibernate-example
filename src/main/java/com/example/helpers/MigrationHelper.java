package com.example.helpers;

import org.flywaydb.core.Flyway;

public class MigrationHelper {
    private MigrationHelper() {
    }

    public static void populateDb(String url, String user, String password) {
        Flyway.configure().dataSource(url, user, password)
                .load().migrate();
    }
}
