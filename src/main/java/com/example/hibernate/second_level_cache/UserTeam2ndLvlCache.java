package com.example.hibernate.second_level_cache;

import com.example.hibernate.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users_team")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserTeam2ndLvlCache extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User2ndLvlCache user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    Team2ndLvlCache team;

    Instant joined;

    String createdBy;

    public void setUser(User2ndLvlCache user) {
        this.user = user;
        user.getUserTeams().add(this);
    }

    public void setTeam(Team2ndLvlCache team) {
        this.team = team;
        team.getUserTeams().add(this);
    }
}
