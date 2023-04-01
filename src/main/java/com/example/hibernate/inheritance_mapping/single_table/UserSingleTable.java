package com.example.hibernate.inheritance_mapping.single_table;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "users_single_table")
@DiscriminatorColumn(name = "user_type")
public abstract class UserSingleTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;
}