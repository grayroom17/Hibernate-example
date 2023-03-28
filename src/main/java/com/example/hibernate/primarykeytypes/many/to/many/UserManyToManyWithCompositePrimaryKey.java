package com.example.hibernate.primarykeytypes.many.to.many;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users_many_to_many_with_composite_primary_key")
public class UserManyToManyWithCompositePrimaryKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_team_with_composite_primary_key",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id"))
    Set<TeamManyToManyWithCompositePrimaryKey> teams = new HashSet<>();

    public void addTeam(TeamManyToManyWithCompositePrimaryKey team) {
        teams.add(team);
        team.getUsers().add(this);
    }
}