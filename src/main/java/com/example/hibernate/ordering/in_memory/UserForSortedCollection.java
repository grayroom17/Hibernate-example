package com.example.hibernate.ordering.in_memory;

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
public class UserForSortedCollection implements Comparable<UserForSortedCollection> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @ManyToOne
    @JoinColumn(name = "company_id")
    CompanyWithSortedCollection company;

    @Override
    public int compareTo(UserForSortedCollection o) {
        return username.compareTo(o.getUsername());
    }
}