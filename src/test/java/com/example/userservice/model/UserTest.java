package com.example.userservice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("User Model Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with constructor")
    void shouldCreateUserWithConstructor() {
        User user = new User("John Doe", "john@example.com", 30);

        assertAll(
                () -> assertThat(user.getName()).isEqualTo("John Doe"),
                () -> assertThat(user.getEmail()).isEqualTo("john@example.com"),
                () -> assertThat(user.getAge()).isEqualTo(30),
                () -> assertThat(user.getId()).isNull()
        );
    }

    @Test
    @DisplayName("Should create user with default constructor and setters")
    void shouldCreateUserWithDefaultConstructorAndSetters() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(1L);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setAge(25);
        user.setCreatedAt(now);

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(1L),
                () -> assertThat(user.getName()).isEqualTo("Jane Doe"),
                () -> assertThat(user.getEmail()).isEqualTo("jane@example.com"),
                () -> assertThat(user.getAge()).isEqualTo(25),
                () -> assertThat(user.getCreatedAt()).isEqualTo(now)
        );
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);

        User user2 = new User("John Doe", "john@example.com", 30);
        user2.setId(1L);

        User user3 = new User("Jane Doe", "jane@example.com", 25);
        user3.setId(2L);

        assertAll(
                () -> assertThat(user1).isEqualTo(user2),
                () -> assertThat(user1).isNotEqualTo(user3),
                () -> assertThat(user1.hashCode()).isEqualTo(user2.hashCode())
        );
    }

    @Test
    @DisplayName("Should have correct toString format")
    void shouldHaveCorrectToStringFormat() {
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 30));

        String toString = user.toString();

        assertThat(toString).contains("id=1")
                .contains("name='John Doe'")
                .contains("email='john@example.com'")
                .contains("age=30");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should handle null and empty names in setters")
    void shouldHandleNullAndEmptyNames(String name) {
        User user = new User();

        assertThatThrownBy(() -> user.setName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "missing@", "@domain.com"})
    @DisplayName("Should validate email format")
    void shouldValidateEmailFormat(String invalidEmail) {
        User user = new User();

        assertThatThrownBy(() -> user.setEmail(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("Should validate age range")
    void shouldValidateAgeRange() {
        User user = new User();

        assertThatThrownBy(() -> user.setAge(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age must be between 0 and 150");

        assertThatThrownBy(() -> user.setAge(151))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age must be between 0 and 150");
    }

    @Test
    @DisplayName("Should handle null age")
    void shouldHandleNullAge() {
        User user = new User();

        user.setAge(null);

        assertThat(user.getAge()).isNull();
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmailFormats() {
        User user1 = new User("Test", "test@example.com", 25);
        User user2 = new User("Test", "test.name@example.com", 25);
        User user3 = new User("Test", "test@sub.example.com", 25);

        assertThat(user1.getEmail()).isEqualTo("test@example.com");
        assertThat(user2.getEmail()).isEqualTo("test.name@example.com");
        assertThat(user3.getEmail()).isEqualTo("test@sub.example.com");
    }

    @Test
    @DisplayName("Should accept valid age values")
    void shouldAcceptValidAgeValues() {
        User user = new User();

        user.setAge(0);
        assertThat(user.getAge()).isEqualTo(0);

        user.setAge(150);
        assertThat(user.getAge()).isEqualTo(150);

        user.setAge(50);
        assertThat(user.getAge()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should handle email case sensitivity")
    void shouldHandleEmailCaseSensitivity() {
        User user = new User();

        user.setEmail("John.Doe@Example.COM");

        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should trim name whitespace")
    void shouldTrimNameWhitespace() {
        User user = new User();

        user.setName("  John Doe  ");

        assertThat(user.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should handle equals with null and different class")
    void shouldHandleEqualsWithNullAndDifferentClass() {
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);

        assertThat(user.equals(null)).isFalse();
        assertThat(user.equals("not a user")).isFalse();
    }

    @Test
    @DisplayName("Should handle hashCode with null id")
    void shouldHandleHashCodeWithNullId() {
        User user = new User("John Doe", "john@example.com", 30);

        assertThat(user.hashCode()).isNotZero();
    }
}