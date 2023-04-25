package com.example.hibernate.dao;

import com.example.hibernate.entity.BaseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Repository<K extends Serializable, E extends BaseEntity<K>> {

    E save(E entity);

    default Optional<E> findById(K id) {
        return findById(id, Collections.emptyMap());
    }

    Optional<E> findById(K id, Map<String, Object> properties);

    List<E> findAll();

    void update(E entity);

    void delete(E entity);
}
