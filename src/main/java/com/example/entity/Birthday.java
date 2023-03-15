package com.example.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Birthday(LocalDate birthdate) {
    public Long getAge() {
        return ChronoUnit.YEARS.between(LocalDate.now(), birthdate);
    }
}
