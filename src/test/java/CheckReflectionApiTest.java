/*
import com.example.entity.UserForOneToManyTests;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
class CheckReflectionApiTest {
    @Test
    void checkReflectionApi() {
        var expectedSql = """
                          insert
                          into users
                          (username, firstname, lastname, birth_date, role, info)
                          values (?, ?, ?, ?, ?, ?)
                          """;

        var sql = """
                  insert
                  into %s
                  (%s)
                  values (%s)
                  """;

        var userClass = UserForOneToManyTests.class;
        var tableName = Optional.ofNullable(userClass.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(userClass.getName());

        var declaredFields = userClass.getDeclaredFields();
        var columnNames = Arrays.stream(declaredFields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName()))
                .collect(Collectors.joining(", "));
        var values = Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));

        var formattedSql = sql.formatted(tableName, columnNames, values);


        Assertions.assertEquals(expectedSql, formattedSql);
    }
}*/
