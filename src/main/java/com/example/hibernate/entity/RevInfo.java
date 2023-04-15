package com.example.hibernate.entity;

import com.example.hibernate.listeners.event_listener.CustomRevisionListener;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RevisionEntity(CustomRevisionListener.class)
@Entity
@Table(name = "revinfo", schema = "aud")
public class RevInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    Long rev;

    @RevisionTimestamp
    @Column(name = "rev_timestamp")
    Long timestamp;

    String userName;
}
