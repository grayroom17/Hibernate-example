package com.example.hibernate.one.to.one.inverseside.fetch.type.lazy;

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
public class UserForOneToOneInverseSideFetchLazy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    ProfileForOneToOneInverseSideWithFetchLazy profile;

    public void setProfile(ProfileForOneToOneInverseSideWithFetchLazy profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}