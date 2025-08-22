package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.User;
import com.kubrafelek.brokagefirm.enums.Role;
import com.kubrafelek.brokagefirm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testCustomer;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testCustomer = new User("testuser", "encodedPassword", Role.CUSTOMER);
        testCustomer.setId(1L);

        testAdmin = new User("admin", "encodedAdminPassword", Role.ADMIN);
        testAdmin.setId(2L);
    }

    @Test
    void testAuthenticate_Success_Customer() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        User result = userService.authenticate("testuser", "password");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(Role.CUSTOMER, result.getRole());
        assertTrue(result.isCustomer());
        assertFalse(result.isAdmin());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password", "encodedPassword");
    }

    @Test
    void testAuthenticate_Success_Admin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("adminpass", "encodedAdminPassword")).thenReturn(true);

        User result = userService.authenticate("admin", "adminpass");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals(Role.ADMIN, result.getRole());
        assertTrue(result.isAdmin());
        assertFalse(result.isCustomer());
        verify(userRepository).findByUsername("admin");
        verify(passwordEncoder).matches("adminpass", "encodedAdminPassword");
    }

    @Test
    void testAuthenticate_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testCustomer));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        User result = userService.authenticate("testuser", "wrongpassword");

        assertNull(result);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        User result = userService.authenticate("nonexistent", "password");

        assertNull(result);
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testCreateUser_Success_Customer() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        User newUser = new User("newuser", "encodedPassword", Role.CUSTOMER);
        newUser.setId(3L);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.createUser("newuser", "password", Role.CUSTOMER);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(Role.CUSTOMER, result.getRole());
        assertTrue(result.isCustomer());
        assertFalse(result.isAdmin());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_Success_Admin() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("adminpass")).thenReturn("encodedAdminPassword");
        User newAdmin = new User("newadmin", "encodedAdminPassword", Role.ADMIN);
        newAdmin.setId(4L);
        when(userRepository.save(any(User.class))).thenReturn(newAdmin);

        User result = userService.createUser("newadmin", "adminpass", Role.ADMIN);

        assertNotNull(result);
        assertEquals("newadmin", result.getUsername());
        assertEquals(Role.ADMIN, result.getRole());
        assertTrue(result.isAdmin());
        assertFalse(result.isCustomer());
        verify(userRepository).existsByUsername("newadmin");
        verify(passwordEncoder).encode("adminpass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameExists() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser("existinguser", "password", Role.CUSTOMER);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testFindById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testCustomer, result.get());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);

        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void testUserRole_DefaultConstructor() {
        User user = new User();
        user.setUsername("defaultuser");
        user.setPassword("password");

        assertEquals(Role.CUSTOMER, user.getRole());
        assertTrue(user.isCustomer());
        assertFalse(user.isAdmin());
    }

    @Test
    void testUserRole_ParameterizedConstructor() {
        User adminUser = new User("admin", "pass", Role.ADMIN);
        User customerUser = new User("customer", "pass", Role.CUSTOMER);

        assertEquals(Role.ADMIN, adminUser.getRole());
        assertTrue(adminUser.isAdmin());
        assertFalse(adminUser.isCustomer());

        assertEquals(Role.CUSTOMER, customerUser.getRole());
        assertTrue(customerUser.isCustomer());
        assertFalse(customerUser.isAdmin());
    }
}
