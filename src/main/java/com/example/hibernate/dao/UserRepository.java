package com.example.hibernate.dao;

import com.example.hibernate.entity.User;
import jakarta.persistence.EntityManager;

public class UserRepository extends BaseRepository<Long, User> {

    public UserRepository(EntityManager entityManager) {
        super(entityManager, User.class);
    }

}
