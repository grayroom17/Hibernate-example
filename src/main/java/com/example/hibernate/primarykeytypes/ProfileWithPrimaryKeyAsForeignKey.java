package com.example.hibernate.primarykeytypes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "profile_primary_key_as_foreign_key")
public class ProfileWithPrimaryKeyAsForeignKey {

    @Id
    @Column(name = "profile_id")
    Long id;

    @Column(length = 2, columnDefinition = "bpchar")
    String language;

    String programmingLanguage;

    @OneToOne
    @PrimaryKeyJoinColumn
    UserForPrimaryKeyAsForeignKeyCheck user;

    public void setUser(UserForPrimaryKeyAsForeignKeyCheck user){
        this.user = user;
        this.id = user.getId();
        user.setProfile(this);
    }
}
