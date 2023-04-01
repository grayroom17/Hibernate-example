package com.example.hibernate.inheritance_mapping.table_per_class;

import com.example.hibernate.entity.ProgrammingLanguage;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ProgrammerTablePerClass extends UserTablePerClass {
    @Enumerated(EnumType.STRING)
    ProgrammingLanguage programmingLanguage;
}