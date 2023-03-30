package com.example.hibernate.one.to.one.owningside.orphan.removal;

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
public class UserForOneToOneOwningSideOrphanRemovalFalse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user")
    ProfileForOneToOneOwningSideOrphanRemovalFalse profile;

    public void setProfile(ProfileForOneToOneOwningSideOrphanRemovalFalse profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}