package com.example.userservice.dao;

import com.example.userservice.model.User;
import com.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        logger.debug("Attempting to save user: {}", user.getEmail());
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            logger.info("User saved successfully with ID: {}", user.getId());
            return user;
        } catch (ConstraintViolationException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Constraint violation while saving user: {}", user.getEmail(), e);
            throw new RuntimeException("Email already exists: " + user.getEmail(), e);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error saving user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user != null) {
                logger.debug("User found by ID {}: {}", id, user.getEmail());
            } else {
                logger.debug("User not found by ID: {}", id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw new RuntimeException("Failed to find user by ID: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.createQuery("FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email.toLowerCase())
                    .uniqueResult();
            if (user != null) {
                logger.debug("User found by email: {}", email);
            } else {
                logger.debug("User not found by email: {}", email);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new RuntimeException("Failed to find user by email: " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        logger.debug("Finding all users");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User u ORDER BY u.createdAt DESC", User.class).list();
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    @Override
    public List<User> findAll(int page, int size) {
        logger.debug("Finding users - page: {}, size: {}", page, size);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User u ORDER BY u.createdAt DESC", User.class)
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .list();
        } catch (Exception e) {
            logger.error("Error finding users with pagination", e);
            throw new RuntimeException("Failed to retrieve users with pagination", e);
        }
    }

    @Override
    public User update(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User mergedUser = session.merge(user);
            tx.commit();
            return mergedUser;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("Deleting user with ID: {}", id);
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                tx.commit();
                logger.info("User deleted successfully with ID: {}", id);
                return true;
            } else {
                logger.warn("User not found for deletion with ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error deleting user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user with ID: " + id, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Checking if user exists with email: {}", email);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", email.toLowerCase())
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", email, e);
            throw new RuntimeException("Failed to check email existence: " + email, e);
        }
    }

    @Override
    public long count() {
        logger.debug("Counting all users");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Error counting users", e);
            throw new RuntimeException("Failed to count users", e);
        }
    }
}