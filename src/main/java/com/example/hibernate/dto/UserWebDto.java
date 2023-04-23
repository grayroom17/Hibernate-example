package com.example.hibernate.dto;

import com.example.hibernate.entity.PersonalInfo;
import com.example.hibernate.entity.Profile;
import com.example.hibernate.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserWebDto {

    Long id;

    String username;

    PersonalInfo personalInfo;

    Role role;

    String info;

    CompanyWebDto company;

    Profile profile;

//    List<UserTeam> userTeams = new ArrayList<>();

//    Set<Payment> payments = new HashSet<>();

}
