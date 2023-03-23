package com.example.hibernate.one.to.many;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "company")
public class CompanyForOneToManyTestsWithNoExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    @OneToMany(mappedBy = "company")
    List<UserForOneToManyTestsNotExcludedManyFieldFromToStringAndEqualsAndHashCodeMethods> users;
}
