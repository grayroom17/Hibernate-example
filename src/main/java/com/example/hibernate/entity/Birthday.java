package com.example.hibernate.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Birthday(LocalDate birthdate) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Long getAge() {
        return ChronoUnit.YEARS.between(LocalDate.now(), birthdate);
    }
}
