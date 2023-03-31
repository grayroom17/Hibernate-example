package com.example.hibernate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class UserTeam extends BaseEntity<Long> {

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    Team team;

    Instant joined;

    String createdBy;

    public void setUser(User user) {
        this.user = user;
        user.getUserTeams().add(this);
    }

    public void setTeam(Team team) {
        this.team = team;
        team.getUserTeams().add(this);
    }
}
