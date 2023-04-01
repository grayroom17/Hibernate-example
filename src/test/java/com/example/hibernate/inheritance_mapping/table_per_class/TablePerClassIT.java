package com.example.hibernate.inheritance_mapping.table_per_class;

import com.example.hibernate.BaseIT;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.example.hibernate.entity.ProgrammingLanguage.JAVA;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class TablePerClassIT extends BaseIT {

    @Test
    void whenPersistChild_thenHibernateGetNextValueFromSequenceAndThenDoInsertToTableMappedByChild() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var programmer = ProgrammerTablePerClass.builder()
                    .username("programmer")
                    .programmingLanguage(JAVA)
                    .build();

            System.setOut(new PrintStream(outContent));
            session.persist(programmer);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("select nextval('table_per_class')"));
            outContent.reset();

            var manager = ManagerTablePerClass.builder()
                    .username("manager")
                    .projectName("project")
                    .build();

            session.persist(manager);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("select nextval('table_per_class')"));
            outContent.reset();

            session.getTransaction().commit();
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("insert into programmer_table_per_class")
                       && query.contains("insert into manager_table_per_class"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void whenPersistParent_thenHibernateGetNextValueFromSequenceAndThenDoInsertToTableMappedByChild() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var programmer = ProgrammerTablePerClass.builder()
                    .username("programmer")
                    .programmingLanguage(JAVA)
                    .build();

            System.setOut(new PrintStream(outContent));
            session.persist(programmer);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("select nextval('table_per_class')"));
            outContent.reset();

            var manager = ManagerTablePerClass.builder()
                    .username("manager")
                    .projectName("project")
                    .build();

            session.persist(manager);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("select nextval('table_per_class')"));
            outContent.reset();

            System.setOut(new PrintStream(outContent));
            session.getTransaction().commit();
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("insert into programmer_table_per_class")
                       && query.contains("insert into manager_table_per_class"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void whenFindChild_thenHibernateDoSelectToTableMappedByChild() {
        try (var session = sessionFactory.openSession()) {

            System.setOut(new PrintStream(outContent));
            var programmer = session.find(ProgrammerTablePerClass.class, 1L);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from programmer_table_per_class"));
            outContent.reset();

            var managerTablePerClass = session.find(ManagerTablePerClass.class, 2L);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from manager_table_per_class"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void whenFindParent_thenHibernateDoUnionSelectToAllTableMappedByChildren() {
        try (var session = sessionFactory.openSession()) {

            System.setOut(new PrintStream(outContent));
            var programmer = session.find(UserTablePerClass.class, 1L);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from programmer_table_per_class")
                       && query.contains("union all")
                       && query.contains("from manager_table_per_class"));
            outContent.reset();

            var manager = session.find(UserTablePerClass.class, 2L);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from programmer_table_per_class")
                       && query.contains("union all")
                       && query.contains("from manager_table_per_class"));
            outContent.reset();
            System.setOut(originalOut);

            assertTrue(programmer instanceof ProgrammerTablePerClass);
            assertTrue(manager instanceof ManagerTablePerClass);
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
