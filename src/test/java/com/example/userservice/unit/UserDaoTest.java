package com.example.userservice.unit;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.model.User;
import com.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User DAO Unit Tests")
class UserDaoTest {

    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;
    private UserDao userDao;
    private User testUser;

    private MockedStatic<HibernateUtil> hibernateUtilMock;

    @BeforeEach
    void setUp() {
        sessionFactory = mock(SessionFactory.class);
        session = mock(Session.class);
        transaction = mock(Transaction.class);

        hibernateUtilMock = mockStatic(HibernateUtil.class);
        hibernateUtilMock.when(HibernateUtil::getSessionFactory).thenReturn(sessionFactory);

        userDao = new UserDaoImpl();
        testUser = new User("John Doe", "john@example.com", 30);
        testUser.setId(1L);
    }

    @AfterEach
    void tearDown() {
        if (hibernateUtilMock != null) {
            hibernateUtilMock.close();
        }
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        User result = userDao.save(testUser);

        assertThat(result).isEqualTo(testUser);
        verify(session).persist(testUser);
        verify(transaction).commit();
    }

    @Test
    @DisplayName("Should handle save error with rollback")
    void shouldHandleSaveErrorWithRollback() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(session).persist(testUser);

        assertThatThrownBy(() -> userDao.save(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to save user");

        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.get(User.class, 1L)).thenReturn(testUser);

        Optional<User> result = userDao.findById(1L);

        assertThat(result).contains(testUser);
        verify(session).get(User.class, 1L);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.get(User.class, 999L)).thenReturn(null);

        Optional<User> result = userDao.findById(999L);

        assertThat(result).isEmpty();
        verify(session).get(User.class, 999L);
    }

    @Test
    @DisplayName("Should handle exception in findById")
    void shouldHandleExceptionInFindById() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.get(User.class, 1L)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userDao.findById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to find user by ID");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        Query<User> userQuery = mock(Query.class);
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter("email", "john@example.com")).thenReturn(userQuery);
        when(userQuery.uniqueResult()).thenReturn(testUser);

        Optional<User> result = userDao.findByEmail("john@example.com");

        assertThat(result).contains(testUser);
        verify(userQuery).setParameter("email", "john@example.com");
        verify(userQuery).uniqueResult();
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        Query<User> userQuery = mock(Query.class);
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter("email", "unknown@example.com")).thenReturn(userQuery);
        when(userQuery.uniqueResult()).thenReturn(null);

        Optional<User> result = userDao.findByEmail("unknown@example.com");

        assertThat(result).isEmpty();
        verify(userQuery).setParameter("email", "unknown@example.com");
        verify(userQuery).uniqueResult();
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        Query<User> userQuery = mock(Query.class);
        List<User> users = Arrays.asList(testUser);
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.list()).thenReturn(users);

        List<User> result = userDao.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testUser);
        verify(userQuery).list();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.merge(testUser)).thenReturn(testUser);

        User result = userDao.update(testUser);

        assertThat(result).isEqualTo(testUser);
        verify(session).merge(testUser);
        verify(transaction).commit();
    }

    @Test
    @DisplayName("Should handle update error with rollback")
    void shouldHandleUpdateErrorWithRollback() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);
        when(session.merge(testUser)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userDao.update(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update user");

        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.get(User.class, 1L)).thenReturn(testUser);

        boolean result = userDao.delete(1L);

        assertThat(result).isTrue();
        verify(session).remove(testUser);
        verify(transaction).commit();
    }

    @Test
    @DisplayName("Should return false when deleting non-existent user")
    void shouldReturnFalseWhenDeletingNonExistentUser() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.get(User.class, 999L)).thenReturn(null);

        boolean result = userDao.delete(999L);

        assertThat(result).isFalse();
        verify(session, never()).remove(any());
    }

    @Test
    @DisplayName("Should handle delete error with rollback")
    void shouldHandleDeleteErrorWithRollback() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.get(User.class, 1L)).thenReturn(testUser);
        when(transaction.isActive()).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(session).remove(testUser);

        assertThatThrownBy(() -> userDao.delete(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to delete user");

        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        Query<Long> longQuery = mock(Query.class);
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("email", "john@example.com")).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(1L);

        boolean result = userDao.existsByEmail("john@example.com");

        assertThat(result).isTrue();
        verify(longQuery).setParameter("email", "john@example.com");
        verify(longQuery).uniqueResult();
    }

    @Test
    @DisplayName("Should return user count")
    void shouldReturnUserCount() {
        Query<Long> longQuery = mock(Query.class);
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(5L);

        long result = userDao.count();

        assertThat(result).isEqualTo(5L);
        verify(longQuery).uniqueResult();
    }

    @Test
    @DisplayName("Should return users with pagination")
    void shouldReturnUsersWithPagination() {
        Query<User> userQuery = mock(Query.class);
        List<User> users = Arrays.asList(testUser);
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setFirstResult(0)).thenReturn(userQuery);
        when(userQuery.setMaxResults(10)).thenReturn(userQuery);
        when(userQuery.list()).thenReturn(users);

        List<User> result = userDao.findAll(1, 10);

        assertThat(result).hasSize(1);
        verify(userQuery).setFirstResult(0);
        verify(userQuery).setMaxResults(10);
        verify(userQuery).list();
    }

    @Test
    @DisplayName("Should handle exception in existsByEmail")
    void shouldHandleExceptionInExistsByEmail() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Long.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userDao.existsByEmail("test@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to check email existence");
    }

    @Test
    @DisplayName("Should handle exception in count")
    void shouldHandleExceptionInCount() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Long.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userDao.count())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to count users");
    }

    @Test
    @DisplayName("Should handle exception in findAll")
    void shouldHandleExceptionInFindAll() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userDao.findAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to retrieve users");
    }

    @Test
    @DisplayName("Should handle exception in findByEmail")
    void shouldHandleExceptionInFindByEmail() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userDao.findByEmail("test@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to find user by email");
    }
}