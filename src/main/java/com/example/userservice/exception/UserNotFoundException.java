package com.example.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    // Оставляем только один конструктор
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}