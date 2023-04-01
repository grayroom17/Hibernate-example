package com.example.hibernate.one.to.one.stack.owerflow;

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
public class UserForOneToOneNotExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    ProfileForOneToOneWithNotExcludedInverseSideFromToStringAndEqualsAndHashCodeMethods profile;

    public void setProfile(ProfileForOneToOneWithNotExcludedInverseSideFromToStringAndEqualsAndHashCodeMethods profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}