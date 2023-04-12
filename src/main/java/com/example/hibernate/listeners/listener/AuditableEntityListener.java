package com.example.hibernate.listeners.listener;

import com.example.hibernate.listeners.listener.entity.AuditableEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AuditableEntityListener {
    @PrePersist
    private void prePersist(AuditableEntity entity) {
        entity.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }

    @PreUpdate
    private void preUpdate(AuditableEntity entity) {
        entity.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
