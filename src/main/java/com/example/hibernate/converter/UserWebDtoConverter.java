package com.example.hibernate.converter;

import com.example.hibernate.dto.UserWebDto;
import com.example.hibernate.entity.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UserWebDtoConverter implements Converter<User, UserWebDto> {

    private final CompanyWebDtoConverter companyConverter;

    @Override
    public UserWebDto toDto(User entity) {
        return UserWebDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .personalInfo(entity.getPersonalInfo())
                .role(entity.getRole())
                .info(entity.getInfo())
                .company(Optional.ofNullable(entity.getCompany())
                        .map(companyConverter::toDto)
                        .orElse(null))
                .profile(entity.getProfile())
                .build();
    }

    @Override
    public User fromDto(UserWebDto dto) {
        return User.builder()
                .username(dto.getUsername())
                .personalInfo(dto.getPersonalInfo())
                .info(dto.getInfo())
                .role(dto.getRole())
                .company(companyConverter.fromDto(dto.getCompany()))
                .profile(dto.getProfile())
                .build();
    }
}
