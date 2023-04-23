package com.example.hibernate.converter;

import com.example.hibernate.dto.CompanyWebDto;
import com.example.hibernate.entity.Company;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;

import java.util.Optional;

@RequiredArgsConstructor
public class CompanyWebDtoConverter implements Converter<Company, CompanyWebDto> {

    private final Session session;

    @Override
    public CompanyWebDto toDto(Company entity) {
        return CompanyWebDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    @Override
    public Company fromDto(CompanyWebDto dto) {
        return Optional.ofNullable(session.find(Company.class, dto.getId()))
                .orElseThrow(() -> new IllegalArgumentException("There is no company wit id " + dto.getId()));
    }
}
