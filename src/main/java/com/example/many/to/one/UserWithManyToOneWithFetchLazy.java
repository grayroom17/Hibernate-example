package com.example.many.to.one;

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
public class UserWithManyToOneWithFetchLazy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Long id;
    @Column(unique = true)
    String username;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    CompanyForManyToOneTests company;
}