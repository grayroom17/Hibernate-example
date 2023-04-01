package com.example.hibernate.inheritance_mapping.table_per_class;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class UserTablePerClass {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tablePerClassGenerator")
    @SequenceGenerator(name = "tablePerClassGenerator", sequenceName = "table_per_class", allocationSize = 1)
    Long id;

    @Column(unique = true)
    String username;
}