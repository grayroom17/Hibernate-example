package com.example.hibernate.inheritance_mapping.single_table;

import com.example.hibernate.BaseIT;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.example.hibernate.entity.ProgrammingLanguage.JAVA;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class SingleTableIT extends BaseIT {

    @Test
    void whenPersistChild_thenHibernateDoInsertToTableMappedByParentAndSetChildDiscriminator() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var programmer = ProgrammerSingleTable.builder()
                    .username("programmer")
                    .programmingLanguage(JAVA)
                    .build();

            System.setOut(new PrintStream(outContent));
            session.persist(programmer);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("insert into users_single_table")
                       && query.contains("'PROGRAMMER'"));
            outContent.reset();

            var manager = ManagerSingleTable.builder()
                    .username("manager")
                    .projectName("project")
                    .build();

            session.persist(manager);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("insert into users_single_table")
                       && query.contains("'MANAGER'"));
            outContent.reset();

            session.getTransaction().commit();
            log.warn(outContent.toString());
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void whenFindChild_thenHibernateDoSelectToTableMappedByParentWithDiscriminatorWhereClause() {
        try (var session = sessionFactory.openSession()) {

            System.setOut(new PrintStream(outContent));
            session.find(ProgrammerSingleTable.class, 1L);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from users_single_table")
                       && query.contains("user_type='PROGRAMMER'"));
            outContent.reset();

            session.find(ManagerSingleTable.class, 2L);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from users_single_table")
                       && query.contains("user_type='MANAGER'"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void whenFindParent_thenHibernateDoSelectToTableMappedByParentWithoutDiscriminatorWhereClause() {
        try (var session = sessionFactory.openSession()) {

            System.setOut(new PrintStream(outContent));
            var programmer = session.find(UserSingleTable.class, 1L);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from users_single_table"));
            outContent.reset();

            var manager = session.find(UserSingleTable.class, 2L);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from users_single_table"));
            outContent.reset();
            System.setOut(originalOut);

            assertTrue(programmer instanceof ProgrammerSingleTable);
            assertTrue(manager instanceof ManagerSingleTable);
        }
    }

    @NotNull
    private String getQuery() {
        return outContent.toString()
                .replaceAll("[\\t\\n\\r]+", " ")
                .replaceAll(" +", " ")
                .trim();
    }
}
