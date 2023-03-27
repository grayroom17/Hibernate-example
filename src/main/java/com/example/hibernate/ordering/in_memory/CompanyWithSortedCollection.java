package com.example.hibernate.ordering.in_memory;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SortNatural;

import java.util.Set;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "company")
public class CompanyWithSortedCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
//    @SortComparator()
    @SortNatural
    Set<UserForSortedCollection> users = new TreeSet<>();

    public void addUser(UserForSortedCollection user) {
        users.add(user);
        user.setCompany(this);
    }
}
