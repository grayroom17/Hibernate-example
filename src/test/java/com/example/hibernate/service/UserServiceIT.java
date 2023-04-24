package com.example.hibernate.service;

import com.example.hibernate.BaseIT;
import com.example.hibernate.converter.CompanyWebDtoConverter;
import com.example.hibernate.converter.UserWebDtoConverter;
import com.example.hibernate.dao.UserRepository;
import com.example.hibernate.dto.CompanyWebDto;
import com.example.hibernate.dto.UserWebDto;
import com.example.hibernate.entity.*;
import com.example.hibernate.listeners.interceptor.TransactionInterceptor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIT extends BaseIT {
    private static Session session;
    private static UserService userService;

    @BeforeAll
    static void initBeans() {
        session = (Session) Proxy.newProxyInstance(SessionFactory.class.getClassLoader(),
                new Class[]{Session.class},
                (proxy, method, args) -> method.invoke(sessionFactory.getCurrentSession(), args));

        UserRepository userRepository = new UserRepository(session);
        CompanyWebDtoConverter companyConverter = new CompanyWebDtoConverter(session);
        UserWebDtoConverter userConverter = new UserWebDtoConverter(companyConverter);
        TransactionInterceptor transactionInterceptor = new TransactionInterceptor(sessionFactory);

        try {
            //noinspection resource
            userService = new ByteBuddy()
                    .subclass(UserService.class)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(transactionInterceptor))
                    .make()
                    .load(UserService.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor(UserRepository.class, UserWebDtoConverter.class)
                    .newInstance(userRepository, userConverter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void create() {
        var userDto = UserWebDto.builder()
                .username("user created through service")
                .role(Role.ADMIN)
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

        var createdUserDto = userService.create(userDto);
        assertNotNull(createdUserDto);
        assertNotNull(createdUserDto.getId());
        var user = userService.findById(createdUserDto.getId()).orElseThrow();

        assertEquals(userDto.getUsername(), createdUserDto.getUsername());
        assertEquals(userDto.getInfo(), createdUserDto.getInfo());
        assertEquals(userDto.getCompany().getId(), createdUserDto.getCompany().getId());
        assertEquals(userDto.getProfile(), createdUserDto.getProfile());

        assertEquals(userDto.getUsername(), user.getUsername());
        assertEquals(userDto.getInfo(), user.getInfo());
        assertEquals(userDto.getCompany().getId(), user.getCompany().getId());
        assertEquals(userDto.getProfile(), user.getProfile());
    }

    @Test
    void findById() {
        var userId = 10L;

        var optionalOfUserDto = userService.findById(userId);

        session.beginTransaction();
        var user = session.find(User.class, userId);
        var company = user.getCompany();
        var companyName = company.getName();
        session.getTransaction().commit();

        assertTrue(optionalOfUserDto.isPresent());
        var userDto = optionalOfUserDto.orElseThrow();
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getUsername(), userDto.getUsername());
        assertEquals(user.getInfo(), userDto.getInfo());
        assertEquals(user.getPersonalInfo(), userDto.getPersonalInfo());
        assertEquals(company.getId(), userDto.getCompany().getId());
        assertEquals(companyName, userDto.getCompany().getName());
        assertEquals(user.getProfile(), userDto.getProfile());
    }
}
