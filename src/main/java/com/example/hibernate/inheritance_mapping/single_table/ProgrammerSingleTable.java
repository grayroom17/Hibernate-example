package com.example.hibernate.inheritance_mapping.single_table;

import com.example.hibernate.entity.ProgrammingLanguage;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@DiscriminatorValue("PROGRAMMER")
public class ProgrammerSingleTable extends UserSingleTable {
    @Enumerated(EnumType.STRING)
    ProgrammingLanguage programmingLanguage;
}