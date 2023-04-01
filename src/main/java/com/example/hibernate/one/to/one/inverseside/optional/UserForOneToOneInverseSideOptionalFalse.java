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
@Table(name = "users")
public class UserForOneToOneInverseSideOptionalFalse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user", optional = false)
    ProfileForOneToOneInverseSideWithOptionalFalse profile;

    public void setProfile(ProfileForOneToOneInverseSideWithOptionalFalse profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}