package com.example.hibernate.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users_team")
public class UserTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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
