package com.example.hibernate.dao;

import com.example.hibernate.entity.BaseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface Repository<K extends Serializable, E extends BaseEntity<K>> {

    E save(E entity);

    Optional<E> findById(K id);

    List<E> findAll();

    void update(E entity);

    void deleteById(K id);
}
