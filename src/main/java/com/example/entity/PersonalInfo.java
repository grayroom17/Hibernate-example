package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class PersonalInfo {
    String firstname;
    String lastname;
    @SuppressWarnings("JpaAttributeTypeInspection")
    @Column(name = "birth_date")
    Birthday birthdate;
}
