package com.example.hibernate.performance.batch_size;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.*;
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
@Table(name = "users")
public class UserPerformanceWithBatchSize extends BaseEntity<Long> {

    @Column(unique = true)
    String username;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    CompanyBatchSize company;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @BatchSize(size = 3)
    @OneToMany(mappedBy = "receiver", fetch = FetchType.EAGER)
    List<PaymentBatchSize> payment = new ArrayList<>();
}