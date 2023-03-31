package com.example.hibernate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Profile extends BaseEntity<Long> {

    @Column(length = 2, columnDefinition = "bpchar")
    String language;

    String programmingLanguage;

    @OneToOne
    @JoinColumn(name = "user_id")
    User user;

    public void setUser(User user) {
        this.user = user;
        user.setProfile(this);
    }
}
