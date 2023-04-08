package com.example.hibernate.performance;

import com.example.hibernate.entity.BaseEntity;
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
public class UserTeamPerformance extends BaseEntity<Long> {

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserPerformanceWithFetchEager user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    TeamPerformance team;

    Instant joined;

    String createdBy;

    public void setUser(UserPerformanceWithFetchEager user) {
        this.user = user;
        user.getUserTeams().add(this);
    }

    public void setTeam(TeamPerformance team) {
        this.team = team;
        team.getUserTeams().add(this);
    }
}
