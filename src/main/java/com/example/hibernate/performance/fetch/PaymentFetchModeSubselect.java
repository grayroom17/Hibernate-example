package com.example.hibernate.performance.fetch;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payment")
public class PaymentFetchModeSubselect extends BaseEntity<Long> {
    @Column(nullable = false)
    Integer amount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id")
    UserPerformanceWithFetchModeSubselect receiver;
}