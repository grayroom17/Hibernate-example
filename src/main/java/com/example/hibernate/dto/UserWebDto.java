package com.example.hibernate.dto;

import com.example.hibernate.entity.PersonalInfo;
import com.example.hibernate.entity.Profile;
import com.example.hibernate.entity.Role;
import com.example.hibernate.validation.ForGroupValidationTest;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    Role role;

    @NotNull(groups = ForGroupValidationTest.class)
    String info;

    CompanyWebDto company;

    Profile profile;

}
