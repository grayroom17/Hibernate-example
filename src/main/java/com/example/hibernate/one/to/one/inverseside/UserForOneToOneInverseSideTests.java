package com.example.hibernate.one.to.one.inverseside;

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
public class UserForOneToOneInverseSideTests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    ProfileForOneToOneInverseSideTests profile;

    public void setProfile(ProfileForOneToOneInverseSideTests profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}