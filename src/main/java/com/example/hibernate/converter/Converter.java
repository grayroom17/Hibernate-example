package com.example.hibernate.converter;

public interface Converter<E, D> {
    D toDto(E entity);
    E fromDto(D dto);
}
