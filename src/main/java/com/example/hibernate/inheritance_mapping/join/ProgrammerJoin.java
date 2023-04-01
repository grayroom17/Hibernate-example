package com.example.hibernate.inheritance_mapping.join;

import com.example.hibernate.entity.ProgrammingLanguage;
import jakarta.persistence.*;
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
@Table(name = "programmer_join_inheritance_strategy")
public class ProgrammerJoin extends UserJoin {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProgrammingLanguage programmingLanguage;
}