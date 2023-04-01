package com.example.hibernate.inheritance_mapping.join;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@PrimaryKeyJoinColumn(name = "user_id")
@Table(name = "manager_join_inheritance_strategy")
public class ManagerJoin extends UserJoin {
    @Column(nullable = false)
    String projectName;
}