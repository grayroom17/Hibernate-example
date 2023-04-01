package com.example.hibernate.one.to.one.owningside.fetch.type.lazy;

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
public class UserForOneToOneOwningSideTestsOwningSideFetchLazy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user")
    ProfileForOneToOneOwningSideTestsWithFetchLazy profile;

    public void setProfile(ProfileForOneToOneOwningSideTestsWithFetchLazy profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}