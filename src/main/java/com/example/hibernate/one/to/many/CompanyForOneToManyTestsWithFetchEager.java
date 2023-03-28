package com.example.hibernate.one.to.many;

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
public class CompanyForOneToManyTestsWithFetchEager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
    Set<UserForOneToManyTestsWithFetchEager> users = new HashSet<>();

    public void addUser(UserForOneToManyTestsWithFetchEager user) {
        users.add(user);
        user.setCompany(this);
    }
}
