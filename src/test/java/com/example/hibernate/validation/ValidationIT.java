package com.example.hibernate.validation;

import com.example.hibernate.BaseIT;
import com.example.hibernate.dto.UserWebDto;
import com.example.hibernate.entity.Role;
import com.example.hibernate.entity.User;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class ValidationIT extends BaseIT {

    @Test
    void givenEntity_whenEntityHaveConstraintViolation_thenHibernateValidatorThrowException() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            User user = User.builder()
                    .username("broken user")
                    .build();

            var constraintViolationException = assertThrows(ConstraintViolationException.class, () -> session.persist(user));
            log.info(constraintViolationException.getMessage());

            session.getTransaction().rollback();
        }
    }

    @Test
    void givenCustomValidator_whenDtoHaveConstraintViolation_thenCustomValidatorThrowException() {
        var dto = UserWebDto.builder()
                .username("broken Dto")
                .build();
        var constraintViolationException = assertThrows(ConstraintViolationException.class, () -> validate(dto));
        log.info(constraintViolationException.getMessage());
    }

    @Test
    void givenValidationGroup_whenDtoHaveConstraintViolationFromGroup_thenCustomValidatorThrowException() {
        var dto = UserWebDto.builder()
                .username("broken Dto")
                .role(Role.USER)
                .build();
        var constraintViolationException = assertThrows(ConstraintViolationException.class,
                () -> validateWithGroup(dto, ForGroupValidationTest.class));
        log.info(constraintViolationException.getMessage());
    }

    private void validate(UserWebDto dto) {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            var validationResult = validator.validate(dto);
            if (!validationResult.isEmpty()) {
                throw new ConstraintViolationException(validationResult);
            }
        }
    }

    private void validateWithGroup(UserWebDto dto, @SuppressWarnings("SameParameterValue") Class<?> group) {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            var validationResult = validator.validate(dto, group);
            if (!validationResult.isEmpty()) {
                throw new ConstraintViolationException(validationResult);
            }
        }
    }
}
