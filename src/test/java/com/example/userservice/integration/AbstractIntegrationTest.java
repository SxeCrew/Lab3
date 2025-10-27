package com.example.userservice.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @BeforeEach
    void setUp() {
        System.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        System.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
    }
}