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
@Table(name = "users_with_composite_primary_key")
public class UserWithCompositePrimaryKey {
    @EmbeddedId
    PersonalInfo personalInfo;
    @Column(unique = true)
    String username;
    @Enumerated(EnumType.STRING)
    Role role;
    @Type(JsonBinaryType.class)
    String info;
}
