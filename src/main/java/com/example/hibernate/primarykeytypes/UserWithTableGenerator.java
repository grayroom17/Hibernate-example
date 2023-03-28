package com.example.hibernate.primarykeytypes;

import com.example.hibernate.entity.PersonalInfo;
import com.example.hibernate.entity.Role;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users_with_table_generator")
public class UserWithTableGenerator {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_generator")
    @TableGenerator(name = "table_generator",
            table = "sequences_table",
            pkColumnName = "table_name",
            valueColumnName = "pk_value",
            allocationSize = 1)
    Long id;
    @Column(unique = true)
    String username;
    @Embedded
    PersonalInfo personalInfo;
    @Enumerated(EnumType.STRING)
    Role role;
    @Type(JsonBinaryType.class)
    String info;
}
