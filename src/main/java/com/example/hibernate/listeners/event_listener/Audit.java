package com.example.hibernate.listeners.event_listener;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Audit extends BaseEntity<Long> {

    String entityId;

    String entityClass;

    String entityContent;

    @Enumerated(EnumType.STRING)
    Operation operation;

    public enum Operation {
        SAVE, UPDATE, DELETE, INSERT
    }
}
