package com.example.hibernate.one.to.one.owningside;

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
public class ProfileForOneToOneOwningSideTestsWithNotExcludedInverseSideFromToStringAndEqualsAndHashCodeMethods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 2, columnDefinition = "bpchar")
    String language;

    String programmingLanguage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserForOneToOneOwningSideTestsNotExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods user;

}
