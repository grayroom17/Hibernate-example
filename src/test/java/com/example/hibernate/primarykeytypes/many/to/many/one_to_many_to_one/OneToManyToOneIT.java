package com.example.hibernate.primarykeytypes.many.to.many.one_to_many_to_one;

import com.example.hibernate.BaseIT;
import com.example.hibernate.many.to.many.one_to_many_to_one.TeamForOneToManyToOneTests;
import com.example.hibernate.many.to.many.one_to_many_to_one.UserForOneToManyToOneTests;
import com.example.hibernate.many.to.many.one_to_many_to_one.UserTeamForOneToManyToOneTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@Slf4j
class OneToManyToOneIT extends BaseIT {

    @Test
    void persist() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var user = session.find(UserForOneToManyToOneTests.class, 2L);
            var team = session.find(TeamForOneToManyToOneTests.class, 1L);

            var userTeam = UserTeamForOneToManyToOneTests.builder()
                    .joined(Instant.now())
                    .createdBy(user.getUsername())
                    .build();

            userTeam.setUser(user);
            userTeam.setTeam(team);

            session.persist(userTeam);
            session.getTransaction().commit();
            log.info("good");
        }
    }

}