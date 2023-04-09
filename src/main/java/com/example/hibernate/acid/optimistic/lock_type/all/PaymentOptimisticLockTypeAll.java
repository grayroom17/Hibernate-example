package com.example.hibernate.acid.optimistic.lock_type.all;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payment")
@OptimisticLocking(type = OptimisticLockType.ALL)
@DynamicUpdate
public class PaymentOptimisticLockTypeAll extends BaseEntity<Long> {
    @Column(nullable = false)
    Integer amount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    UserOptimisticLockTypeAll receiver;
}