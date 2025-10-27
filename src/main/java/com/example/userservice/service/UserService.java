package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.exception.EmailAlreadyExistsException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUser(String name, String email, Integer age) {
        logger.info("Creating new user: {}", email);

        // Проверяем существование email
        if (userDao.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(name, email, age);
        return userDao.save(user);
    }

    public User getUserById(Long id) {
        logger.debug("Retrieving user by ID: {}", id);
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public Optional<User> findUserByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userDao.findByEmail(email);
    }

    public List<User> getAllUsers() {
        logger.debug("Retrieving all users");
        return userDao.findAll();
    }

    public List<User> getUsersWithPagination(int page, int size) {
        logger.debug("Retrieving users with pagination - page: {}, size: {}", page, size);
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("Page and size must be positive integers");
        }
        return userDao.findAll(page, size);
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        logger.info("Updating user with ID: {}", id);

        // Проверяем существование пользователя
        User existingUser = userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Проверяем email на уникальность (если изменился)
        if (!existingUser.getEmail().equalsIgnoreCase(email) &&
                userDao.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        // Обновляем поля
        if (name != null && !name.trim().isEmpty()) {
            existingUser.setName(name);
        }
        if (email != null && !email.trim().isEmpty()) {
            existingUser.setEmail(email);
        }
        if (age != null) {
            existingUser.setAge(age);
        }

        return userDao.update(existingUser);
    }

    public boolean deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        return userDao.delete(id);
    }

    public boolean userExists(Long id) {
        return userDao.findById(id).isPresent();
    }

    public boolean emailExists(String email) {
        return userDao.existsByEmail(email);
    }

    public long getUserCount() {
        return userDao.count();
    }
}