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
@Table(name = "users")
public class UserForOneToOneOwningSideTests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user")
    ProfileForOneToOneOwningSideTests profile;

    public void setProfile(ProfileForOneToOneOwningSideTests profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}