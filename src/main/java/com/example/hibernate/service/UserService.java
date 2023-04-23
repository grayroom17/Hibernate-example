package com.example.hibernate.service;

import com.example.hibernate.converter.UserWebDtoConverter;
import com.example.hibernate.dao.UserRepository;
import com.example.hibernate.dto.UserWebDto;
import jakarta.persistence.EntityGraph;
import lombok.RequiredArgsConstructor;
import org.hibernate.graph.GraphSemantic;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserWebDtoConverter userConverter;


    public UserWebDto create(UserWebDto dto) {
        var userToSave = userConverter.fromDto(dto);
        var user = userRepository.save(userToSave);
        return userConverter.toDto(user);
    }

    public Optional<UserWebDto> findById(Long id) {
        EntityGraph<?> graph = userRepository.getEntityManager().getEntityGraph("graphWithCompanyAndProfile");

        Map<String, Object> properties = Map.of(GraphSemantic.LOAD.getJakartaHintName(), graph);

        var user = userRepository.findById(id, properties);
        return user.map(userConverter::toDto);
    }

    public boolean deleteById(Long id) {
        var user = userRepository.findById(id);
        user.ifPresent(userRepository::delete);
        return user.isPresent();
    }
}
