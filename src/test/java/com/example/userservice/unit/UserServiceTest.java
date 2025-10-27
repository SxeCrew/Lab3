package com.example.userservice.unit;

import com.example.userservice.dao.UserDao;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
        testUser = new User("John Doe", "john@example.com", 30);
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        when(userDao.existsByEmail("john@example.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser("John Doe", "john@example.com", 30);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(1L);
        verify(userDao).existsByEmail("john@example.com");
        verify(userDao).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void shouldThrowExceptionWhenEmailExists() {
        when(userDao.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("John Doe", "john@example.com", 30))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists: john@example.com");

        verify(userDao).existsByEmail("john@example.com");
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void shouldFindUserByIdSuccessfully() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserById(1L);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(1L);
        verify(userDao).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: 999");

        verify(userDao).findById(999L);
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void shouldFindUserByEmailSuccessfully() {
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.findUserByEmail("john@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
        verify(userDao).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        when(userDao.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findUserByEmail("unknown@example.com");

        assertThat(foundUser).isEmpty();
        verify(userDao).findByEmail("unknown@example.com");
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        List<User> users = Arrays.asList(testUser, new User("Jane Doe", "jane@example.com", 25));
        when(userDao.findAll()).thenReturn(users);

        List<User> allUsers = userService.getAllUsers();

        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getName).contains("John Doe", "Jane Doe");
        verify(userDao).findAll();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        User updatedUser = new User("John Updated", "john.updated@example.com", 35);
        updatedUser.setId(1L);

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(userDao.update(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(1L, "John Updated", "john.updated@example.com", 35);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@example.com");
        assertThat(result.getAge()).isEqualTo(35);
        verify(userDao).findById(1L);
        verify(userDao).existsByEmail("john.updated@example.com");
        verify(userDao).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, "New Name", "new@example.com", 40))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: 999");

        verify(userDao).findById(999L);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        when(userDao.delete(1L)).thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertThat(result).isTrue();
        verify(userDao).delete(1L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent user")
    void shouldReturnFalseWhenDeletingNonExistentUser() {
        when(userDao.delete(999L)).thenReturn(false);

        boolean result = userService.deleteUser(999L);

        assertThat(result).isFalse();
        verify(userDao).delete(999L);
    }

    @Test
    @DisplayName("Should check if user exists")
    void shouldCheckIfUserExists() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        boolean exists = userService.userExists(1L);

        assertThat(exists).isTrue();
        verify(userDao).findById(1L);
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        when(userDao.existsByEmail("john@example.com")).thenReturn(true);

        boolean exists = userService.emailExists("john@example.com");

        assertThat(exists).isTrue();
        verify(userDao).existsByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return user count")
    void shouldReturnUserCount() {
        when(userDao.count()).thenReturn(5L);

        long count = userService.getUserCount();

        assertThat(count).isEqualTo(5L);
        verify(userDao).count();
    }

    @Test
    @DisplayName("Should return users with pagination")
    void shouldReturnUsersWithPagination() {
        List<User> users = Arrays.asList(testUser);
        when(userDao.findAll(1, 10)).thenReturn(users);

        List<User> result = userService.getUsersWithPagination(1, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(userDao).findAll(1, 10);
    }

    @Test
    @DisplayName("Should throw exception for invalid pagination parameters")
    void shouldThrowExceptionForInvalidPagination() {
        assertThatThrownBy(() -> userService.getUsersWithPagination(0, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Page and size must be positive integers");

        assertThatThrownBy(() -> userService.getUsersWithPagination(1, -1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Page and size must be positive integers");

        verify(userDao, never()).findAll(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should update user with partial data")
    void shouldUpdateUserWithPartialData() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, null, null, null);

        assertThat(result).isEqualTo(testUser);
        verify(userDao).findById(1L);
        verify(userDao).update(any(User.class));
    }

    @Test
    @DisplayName("Should update user with same email")
    void shouldUpdateUserWithSameEmail() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, "New Name", "john@example.com", null);

        assertThat(result).isEqualTo(testUser);
        verify(userDao).findById(1L);
        verify(userDao, never()).existsByEmail(anyString());
        verify(userDao).update(any(User.class));
    }
}