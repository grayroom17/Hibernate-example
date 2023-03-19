package com.example.primarykeytypes;

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
@Table(name = "users_with_sequence")
public class UserWithSequence {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_with_sequence_generator")
    @SequenceGenerator(name = "users_with_sequence_generator", sequenceName = "users_with_sequence_id_seq", allocationSize = 1)
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
