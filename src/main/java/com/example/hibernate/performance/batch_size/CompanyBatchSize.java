package com.example.hibernate.performance.batch_size;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

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
@BatchSize(size = 2)
public class CompanyBatchSize extends BaseEntity<Long> {

    @Column(unique = true)
    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "company")
    List<UserPerformanceWithBatchSize> users = new ArrayList<>();

    public void addUser(UserPerformanceWithBatchSize user) {
        users.add(user);
        user.setCompany(this);
    }
}
