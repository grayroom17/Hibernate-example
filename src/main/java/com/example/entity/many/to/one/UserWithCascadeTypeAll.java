package com.example.entity.many.to.one;

import com.example.entity.Company;
import com.example.entity.PersonalInfo;
import com.example.entity.Role;
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
public class UserWithCascadeTypeAll {
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
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id")
    Company company;
}