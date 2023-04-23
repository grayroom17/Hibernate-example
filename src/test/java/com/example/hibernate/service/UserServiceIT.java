package com.example.hibernate.service;

import com.example.hibernate.BaseIT;
import com.example.hibernate.converter.CompanyWebDtoConverter;
import com.example.hibernate.converter.UserWebDtoConverter;
import com.example.hibernate.dao.UserRepository;
import com.example.hibernate.dto.CompanyWebDto;
import com.example.hibernate.dto.UserWebDto;
import com.example.hibernate.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIT extends BaseIT {
    private static final Session SESSION;
    private static final UserService USER_SERVICE;

    static {
        SESSION = (Session) Proxy.newProxyInstance(SessionFactory.class.getClassLoader(),
                new Class[]{Session.class},
                (proxy, method, args) -> method.invoke(sessionFactory.getCurrentSession(), args));

        UserRepository userRepository = new UserRepository(SESSION);

        CompanyWebDtoConverter companyConverter = new CompanyWebDtoConverter(SESSION);
        UserWebDtoConverter userConverter = new UserWebDtoConverter(companyConverter);

        USER_SERVICE = new UserService(userRepository, userConverter);
    }

    @Test
    void create() {
        var userDto = UserWebDto.builder()
                .username("user created through service")
                .role(Role.ADMIN)
//                .profile(Profile.builder()
//                        .programmingLanguage("Delphi")
//                        .language("DE")
//                        .build())
                .info("""
                      {"age": 25}""")
                .personalInfo(PersonalInfo.builder()
                        .firstname("Jonas")
                        .lastname("Torvald")
                        .birthdate(new Birthday(LocalDate.of(1983, 10, 11)))
                        .build())
                .company(CompanyWebDto.builder()
                        .id(1L)
                        .build())
                .build();

        SESSION.beginTransaction();
        var createdUserDto = USER_SERVICE.create(userDto);
        assertNotNull(createdUserDto);
        assertNotNull(createdUserDto.getId());
        var user = SESSION.find(User.class, createdUserDto.getId());
        SESSION.getTransaction().commit();

        assertEquals(userDto.getUsername(), createdUserDto.getUsername());
        assertEquals(userDto.getInfo(), createdUserDto.getInfo());
//        assertEquals(userDto.getPersonalInfo(), createdUserDto.getPersonalInfo());
        assertEquals(userDto.getCompany().getId(), createdUserDto.getCompany().getId());
        assertEquals(userDto.getProfile(), createdUserDto.getProfile());

        assertEquals(userDto.getUsername(), user.getUsername());
        assertEquals(userDto.getInfo(), user.getInfo());
//        assertEquals(userDto.getPersonalInfo(), user.getPersonalInfo());
        assertEquals(userDto.getCompany().getId(), user.getCompany().getId());
        assertEquals(userDto.getProfile(), user.getProfile());
    }

    @Test
    void findById() {
        var userId = 10L;

        SESSION.beginTransaction();
        var optionalOfUserDto = USER_SERVICE.findById(userId);
        var user = SESSION.find(User.class, userId);
        SESSION.getTransaction().commit();

        assertTrue(optionalOfUserDto.isPresent());
        var userDto = optionalOfUserDto.orElseThrow();
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getUsername(), userDto.getUsername());
        assertEquals(user.getInfo(), userDto.getInfo());
        assertEquals(user.getPersonalInfo(), userDto.getPersonalInfo());
        assertEquals(user.getCompany().getId(), userDto.getCompany().getId());
        assertEquals(user.getCompany().getName(), userDto.getCompany().getName());
        assertEquals(user.getProfile(), userDto.getProfile());
    }
}
