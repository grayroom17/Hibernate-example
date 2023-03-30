package com.example.hibernate.one.to.one.owningside.cascade.type;

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
public class UserForOneToOneOwningSideTestsWithoutCascadeTypes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user")
    ProfileForOneToOneOwningSideTestsWithoutCascadeTypes profile;

    public void setProfile(ProfileForOneToOneOwningSideTestsWithoutCascadeTypes profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}