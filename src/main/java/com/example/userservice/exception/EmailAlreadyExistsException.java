package com.example.userservice.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    // Оставляем только один конструктор
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}