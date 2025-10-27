package com.example.userservice.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hibernate Util Tests")
class HibernateUtilTest {

    @BeforeEach
    void setUp() {
        HibernateUtil.recreateSessionFactory();
    }

    @AfterEach
    void tearDown() {
        HibernateUtil.shutdown();
    }

    @Test
    @DisplayName("Should get session factory")
    void shouldGetSessionFactory() {
        assertThat(HibernateUtil.getSessionFactory()).isNotNull();
    }

    @Test
    @DisplayName("Should check if session factory is open")
    void shouldCheckIfSessionFactoryIsOpen() {
        assertThat(HibernateUtil.isSessionFactoryOpen()).isTrue();
    }

    @Test
    @DisplayName("Should shutdown session factory")
    void shouldShutdownSessionFactory() {
        HibernateUtil.shutdown();
        assertThat(HibernateUtil.isSessionFactoryOpen()).isFalse();
    }

    @Test
    @DisplayName("Should recreate session factory")
    void shouldRecreateSessionFactory() {
        HibernateUtil.shutdown();
        assertThat(HibernateUtil.isSessionFactoryOpen()).isFalse();

        HibernateUtil.recreateSessionFactory();
        assertThat(HibernateUtil.isSessionFactoryOpen()).isTrue();
    }
}