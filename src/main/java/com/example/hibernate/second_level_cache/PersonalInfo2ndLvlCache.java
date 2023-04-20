package com.example.hibernate.second_level_cache;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class PersonalInfo2ndLvlCache implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    String firstname;
    String lastname;
    @Column(name = "birth_date")
    LocalDate birthdate;
}
