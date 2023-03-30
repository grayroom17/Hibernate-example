package com.example.hibernate.one.to.one.inverseside.cascade.types;

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
public class UserForOneToOneInverseSideWithoutCascadeTypes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user")
    ProfileForOneToOneInverseSideWithoutCascadeTypes profile;

    public void setProfile(ProfileForOneToOneInverseSideWithoutCascadeTypes profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}