package com.example.hibernate.many.to.one;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "company")
public class CompanyForManyToOneOwningSideWithoutCascadeTypes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "company")
    Set<UserManyToOneWithoutCascadeTypes> users;

    public void addUser(UserManyToOneWithoutCascadeTypes user) {
        users.add(user);
        user.setCompany(this);
    }
}
