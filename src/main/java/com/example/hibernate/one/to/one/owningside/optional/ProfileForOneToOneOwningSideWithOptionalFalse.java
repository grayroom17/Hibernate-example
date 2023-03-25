package com.example.hibernate.one.to.one.owningside.optional;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "profile")
public class ProfileForOneToOneOwningSideWithOptionalFalse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 2, columnDefinition = "bpchar")
    String language;

    String programmingLanguage;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    UserForOneToOneOwningSideOptionalFalse user;

}
