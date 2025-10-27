package com.example.userservice;

import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("App Tests")
class AppTest {

    @Mock
    private UserService userService;

    private App app;
    private InputStream originalSystemIn;

    @BeforeEach
    void setUp() {
        originalSystemIn = System.in;
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
    }

    @Test
    @DisplayName("Should create user from menu")
    void shouldCreateUserFromMenu() {
        String input = "1\nJohn Doe\njohn@example.com\n30\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        User user = new User("John Doe", "john@example.com", 30);
        when(userService.createUser("John Doe", "john@example.com", 30)).thenReturn(user);

        app.start();

        verify(userService).createUser("John Doe", "john@example.com", 30);
    }

    @Test
    @DisplayName("Should list users from menu")
    void shouldListUsersFromMenu() {
        String input = "2\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        when(userService.getAllUsers()).thenReturn(List.of());

        app.start();

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should get user by ID from menu")
    void shouldGetUserByIdFromMenu() {
        String input = "3\n1\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        User user = new User("John Doe", "john@example.com", 30);
        when(userService.getUserById(1L)).thenReturn(user);

        app.start();

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("Should get user by email from menu")
    void shouldGetUserByEmailFromMenu() {
        String input = "4\njohn@example.com\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        when(userService.findUserByEmail("john@example.com")).thenReturn(Optional.empty());

        app.start();

        verify(userService).findUserByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should update user from menu")
    void shouldUpdateUserFromMenu() {
        String input = "5\n1\n\n\n\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        User user = new User("John Doe", "john@example.com", 30);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.updateUser(eq(1L), any(), any(), any())).thenReturn(user);

        app.start();

        verify(userService).getUserById(1L);
        verify(userService).updateUser(eq(1L), any(), any(), any());
    }

    @Test
    @DisplayName("Should delete user from menu")
    void shouldDeleteUserFromMenu() {
        String input = "6\n1\nDELETE\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        User user = new User("John Doe", "john@example.com", 30);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.deleteUser(1L)).thenReturn(true);

        app.start();

        verify(userService).getUserById(1L);
        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("Should show statistics from menu")
    void shouldShowStatisticsFromMenu() {
        String input = "7\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        when(userService.getUserCount()).thenReturn(0L);

        app.start();

        verify(userService).getUserCount();
    }

    @Test
    @DisplayName("Should handle invalid menu option")
    void shouldHandleInvalidMenuOption() {
        String input = "invalid\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        app.start();

        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("Should handle user creation error")
    void shouldHandleUserCreationError() {
        String input = "1\nJohn Doe\njohn@example.com\n30\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        when(userService.createUser("John Doe", "john@example.com", 30))
                .thenThrow(new RuntimeException("Email already exists"));

        app.start();

        verify(userService).createUser("John Doe", "john@example.com", 30);
    }

    @Test
    @DisplayName("Should handle user not found by ID")
    void shouldHandleUserNotFoundById() {
        String input = "3\n999\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found with id: 999"));

        app.start();

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("Should handle deletion cancellation")
    void shouldHandleDeletionCancellation() {
        String input = "6\n1\nNO\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app = new App(userService);

        User user = new User("John Doe", "john@example.com", 30);
        when(userService.getUserById(1L)).thenReturn(user);

        app.start();

        verify(userService).getUserById(1L);
        verify(userService, never()).deleteUser(1L);
    }
}