package com.example.hibernate.inheritance_mapping.single_table;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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
@DiscriminatorValue("MANAGER")
public class ManagerSingleTable extends UserSingleTable {
    String projectName;
}