package com.example.entity;

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
@Table(name = "users")
public class UserWithManyToOneWithOptionalFalse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String username;
    @Embedded
    PersonalInfo personalInfo;
    @Enumerated(EnumType.STRING)
    Role role;
    @Type(JsonBinaryType.class)
    String info;
    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    Company company;
}