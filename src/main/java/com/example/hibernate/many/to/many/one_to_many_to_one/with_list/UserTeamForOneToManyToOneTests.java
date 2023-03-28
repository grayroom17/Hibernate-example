package com.example.hibernate.many.to.many.one_to_many_to_one.with_list;

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
public class UserTeamForOneToManyToOneTests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserForOneToManyToOneTests user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    TeamForOneToManyToOneTests team;

    Instant joined;

    String createdBy;

    public void setUser(UserForOneToManyToOneTests user) {
        this.user = user;
        user.getUserTeams().add(this);
    }

    public void setTeam(TeamForOneToManyToOneTests team) {
        this.team = team;
        team.getUserTeams().add(this);
    }
}
