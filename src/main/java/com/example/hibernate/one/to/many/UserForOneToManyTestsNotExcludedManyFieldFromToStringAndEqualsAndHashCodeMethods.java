package com.example.hibernate.one.to.many;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserForOneToManyTestsNotExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @ManyToOne
    @JoinColumn(name = "company_id")
    CompanyForOneToManyTestsWithNoExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods company;
}