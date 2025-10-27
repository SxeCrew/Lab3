package com.example.userservice.integration;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User DAO Simple Integration Tests")
class UserDaoIntegrationTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
    }

    @Test
    @DisplayName("User DAO should be initialized")
    void userDaoShouldBeInitialized() {
        assertThat(userDao).isNotNull();
    }

    @Test
    @DisplayName("Should handle optional operations")
    void shouldHandleOptionalOperations() {
        Optional<User> result = userDao.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle list operations")
    void shouldHandleListOperations() {
        List<User> result = userDao.findAll();
        assertThat(result).isNotNull();
    }
}