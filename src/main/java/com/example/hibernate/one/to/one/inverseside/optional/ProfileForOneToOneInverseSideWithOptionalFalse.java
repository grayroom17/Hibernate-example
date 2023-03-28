package com.example.hibernate.one.to.one.inverseside.optional;

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
public class ProfileForOneToOneInverseSideWithOptionalFalse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 2, columnDefinition = "bpchar")
    String language;

    String programmingLanguage;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JoinColumn(name = "user_id")
    UserForOneToOneInverseSideOptionalFalse user;

}
