package com.example.hibernate.inheritance_mapping.join;

import com.example.hibernate.BaseIT;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.example.hibernate.entity.ProgrammingLanguage.JAVA;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class JoinIT extends BaseIT {

    @Test
    void whenPersistChild_thenHibernateDoFirstInsertToTableMappedByParentAndThenDoInsertToTableMappedByChild() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var programmer = ProgrammerJoin.builder()
                    .username("programmer")
                    .programmingLanguage(JAVA)
                    .build();

            System.setOut(new PrintStream(outContent));
            session.persist(programmer);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("insert into users_join_inheritance_strategy")
                       && query.contains("insert into programmer_join_inheritance_strategy"));
            outContent.reset();

            var manager = ManagerJoin.builder()
                    .username("manager")
                    .projectName("project")
                    .build();

            session.persist(manager);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("insert into users_join_inheritance_strategy")
                       && query.contains("insert into manager_join_inheritance_strategy"));
            outContent.reset();

            session.getTransaction().commit();
            log.warn(outContent.toString());
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void whenFindChild_thenHibernateDoSelectToTableMappedByChildWithInnerJoinToTableMappedByParent() {
        try (var session = sessionFactory.openSession()) {

            System.setOut(new PrintStream(outContent));
            session.find(ProgrammerJoin.class, 1L);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from programmer_join_inheritance_strategy")
                       && query.contains("join users_join_inheritance_strategy"));
            outContent.reset();

            session.find(ManagerJoin.class, 2L);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from manager_join_inheritance_strategy")
                       && query.contains("join users_join_inheritance_strategy"));
            outContent.reset();
            System.setOut(originalOut);
        }
    }

    @Test
    void whenFindParent_thenHibernateDoSelectWithCaseToTableMappedByParentWithLeftJoinToAllTablesMappedByChildren() {
        try (var session = sessionFactory.openSession()) {

            System.setOut(new PrintStream(outContent));
            var programmer = session.find(UserJoin.class, 1L);
            var query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from users_join_inheritance_strategy")
                       && query.contains("case")
                       && query.contains("left join programmer_join_inheritance_strategy")
                       && query.contains("left join manager_join_inheritance_strategy"));
            outContent.reset();

            var manager = session.find(UserJoin.class, 2L);
            query = getQuery();
            log.warn(outContent.toString());
            assertTrue(query.contains("from users_join_inheritance_strategy")
                       && query.contains("case")
                       && query.contains("left join programmer_join_inheritance_strategy")
                       && query.contains("left join manager_join_inheritance_strategy"));
            outContent.reset();
            System.setOut(originalOut);

            assertTrue(programmer instanceof ProgrammerJoin);
            assertTrue(manager instanceof ManagerJoin);
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
