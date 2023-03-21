package com.example.one.to.many;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "company")
public class CompanyWithOneToManyWithFetchEager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
    Set<UserForOneToManyFetchEagerTests> users = new HashSet<>();

    public void addUser(UserForOneToManyFetchEagerTests user) {
        users.add(user);
        user.setCompany(this);
    }
}
