package com.example.hibernate.listeners.listener.entity;

import com.example.hibernate.listeners.listener.AuditableEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditableEntityListener.class)
public class AuditableEntity {
    Instant createdAt;
    Instant updatedAt;
}
