package com.example.hibernate.performance.fetch;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
public class UserPerformanceWithFetchModeSubselect extends BaseEntity<Long> {

    @Column(unique = true)
    String username;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @Fetch(value = FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "receiver", fetch = FetchType.EAGER)
    List<PaymentFetchModeSubselect> payment = new ArrayList<>();
}