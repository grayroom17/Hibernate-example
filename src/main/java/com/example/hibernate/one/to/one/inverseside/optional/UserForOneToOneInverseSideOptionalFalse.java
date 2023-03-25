package com.example.hibernate.one.to.one.inverseside.optional;

import com.example.hibernate.entity.Company;
import com.example.hibernate.entity.PersonalInfo;
import com.example.hibernate.entity.Role;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

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

    @Embedded
    PersonalInfo personalInfo;

    @Enumerated(EnumType.STRING)
    Role role;

    @Type(JsonBinaryType.class)
    String info;

    @ManyToOne
    @JoinColumn(name = "company_id")
    Company company;

    @OneToOne(mappedBy = "user", optional = false)
    ProfileForOneToOneInverseSideWithOptionalFalse profile;

    public void setProfile(ProfileForOneToOneInverseSideWithOptionalFalse profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}