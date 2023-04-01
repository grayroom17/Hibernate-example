package com.example.hibernate.one.to.one.inverseside.orhan.removal;

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
public class UserForOneToOneInverseSideOrphanRemovalTrue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user", orphanRemoval = true)
    ProfileForOneToOneInverseSideWithOrphanRemovalTrue profile;

    public void setProfile(ProfileForOneToOneInverseSideWithOrphanRemovalTrue profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}