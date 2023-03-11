package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    String username;
    String firstname;
    String lastname;
    @SuppressWarnings("JpaAttributeTypeInspection")
    @Column(name = "birth_date")
    Birthday birthdate;
    @Enumerated(EnumType.STRING)
    Role role;
}
