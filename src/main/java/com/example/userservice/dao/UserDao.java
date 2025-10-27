package com.example.userservice.dao;

import com.example.userservice.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findAll(int page, int size);
    User update(User user);
    boolean delete(Long id);
    boolean existsByEmail(String email);
    long count();
}