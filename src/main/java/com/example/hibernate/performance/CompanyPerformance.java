package com.example.hibernate.performance;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "company")
public class CompanyPerformance extends BaseEntity<Long> {

    @Column(unique = true)
    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "company")
    List<UserPerformanceWithFetchEager> users = new ArrayList<>();

    public void addUser(UserPerformanceWithFetchEager user) {
        users.add(user);
        user.setCompany(this);
    }
}
