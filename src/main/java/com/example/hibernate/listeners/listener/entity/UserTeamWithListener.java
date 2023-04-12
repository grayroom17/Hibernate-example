package com.example.hibernate.listeners.listener.entity;

import com.example.hibernate.entity.BaseEntity;
import com.example.hibernate.listeners.listener.CountableEntityListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users_team")
@EntityListeners(CountableEntityListener.class)
public class UserTeamWithListener extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserWithListener user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    TeamWithListener team;

    Instant joined;

    String createdBy;

    public void setUser(UserWithListener user) {
        this.user = user;
        user.getUserTeams().add(this);
    }

    public void setTeam(TeamWithListener team) {
        this.team = team;
        team.getUserTeams().add(this);
    }
}
