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
@Table(name = "users")
public class UserForOneToOneOwningSideOptionalFalse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user")
    ProfileForOneToOneOwningSideWithOptionalFalse profile;

    public void setProfile(ProfileForOneToOneOwningSideWithOptionalFalse profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}