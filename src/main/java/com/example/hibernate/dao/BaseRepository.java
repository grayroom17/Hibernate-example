package com.example.hibernate.dao;

import com.example.hibernate.entity.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BaseRepository<K extends Serializable, E extends BaseEntity<K>> implements Repository<K, E> {

    private final SessionFactory sessionFactory;
    private final Class<E> clazz;

    @Override
    public E save(E entity) {
        try (var session = sessionFactory.openSession()) {
            session.persist(entity);
            return entity;
        }
    }

    @Override
    public Optional<E> findById(K id) {
        try (var session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.find(clazz, id));
        }
    }

    @Override
    public List<E> findAll() {
        try (var session = sessionFactory.openSession()) {
            var criteria = session.getCriteriaBuilder().createQuery(clazz);
            criteria.from(clazz);
            return session.createQuery(criteria).getResultList();
        }
    }

    @Override
    public void update(E entity) {
        try (var session = sessionFactory.openSession()) {
            session.merge(entity);
        }
    }

    @Override
    public void deleteById(K id) {
        try (var session = sessionFactory.openSession()) {
            session.remove(id);
        }
    }
}
