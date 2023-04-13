package com.example.hibernate.listeners.callback;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class UserCallback extends BaseEntity<Long> {

    @Column(unique = true)
    String username;

    Instant createdAt;

    Instant updatedAt;

    @PrePersist
    private void prePersist() {
        setCreatedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }

    @PreUpdate
    private void preUpdate() {
        setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}