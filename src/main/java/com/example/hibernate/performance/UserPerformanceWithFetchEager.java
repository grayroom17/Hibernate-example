package com.example.hibernate.performance;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class UserPerformanceWithFetchEager extends BaseEntity<Long> {

    @Column(unique = true)
    String username;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    CompanyPerformance company;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    Set<UserTeamPerformance> userTeams = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "receiver", fetch = FetchType.EAGER)
    List<PaymentPerformance> payment = new ArrayList<>();
}