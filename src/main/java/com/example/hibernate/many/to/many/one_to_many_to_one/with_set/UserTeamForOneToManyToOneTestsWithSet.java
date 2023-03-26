package com.example.hibernate.many.to.many.one_to_many_to_one.with_set;

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
public class UserTeamForOneToManyToOneTestsWithSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserForOneToManyToOneTestsWithSet user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    TeamForOneToManyToOneTestsWithSet team;

    Instant joined;

    String createdBy;

    public void setUser(UserForOneToManyToOneTestsWithSet user) {
        this.user = user;
        user.getUserTeams().add(this);
    }

    public void setTeam(TeamForOneToManyToOneTestsWithSet team) {
        this.team = team;
        team.getUserTeams().add(this);
    }
}
