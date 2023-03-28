package com.example.hibernate.ordering.in_memory.map;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SortNatural;

import java.util.Map;
import java.util.TreeMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "company")
public class CompanyWithMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @MapKey(name = "username")
    @SortNatural
    Map<String, UserForMap> users = new TreeMap<>();

    public void addUser(UserForMap user) {
        users.put(user.getUsername(), user);
        user.setCompany(this);
    }
}
