package com.BuonoRiccitiello.twitter.service;

import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per il servizio di autenticazione.
 *
 * <p><strong>Test coverage:</strong></p>
 * <ul>
 *   <li>Codifica password con BCrypt</li>
 *   <li>Verifica password corretta</li>
 *   <li>Verifica password non corretta</li>
 *   <li>Login riuscito</li>
 *   <li>Login fallito - username non trovato</li>
 *   <li>Login fallito - password errata</li>
 * </ul>
 */
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(passwordEncoder, userRepository);
    }

    @Test
    @DisplayName("Dovrebbe codificare una password con BCrypt")
    void testEncodePassword() {
        // Arrange
        String rawPassword = "myPassword123";

        // Act
        String encodedPassword = authService.encodePassword(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"));
    }

    @Test
    @DisplayName("Dovrebbe verificare che una password corretta corrisponde")
    void testVerifyCorrectPassword() {
        // Arrange
        String rawPassword = "myPassword123";
        String encodedPassword = authService.encodePassword(rawPassword);

        // Act
        boolean isValid = authService.verifyPassword(rawPassword, encodedPassword);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Dovrebbe verificare che una password errata non corrisponde")
    void testVerifyIncorrectPassword() {
        // Arrange
        String rawPassword = "myPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = authService.encodePassword(rawPassword);

        // Act
        boolean isValid = authService.verifyPassword(wrongPassword, encodedPassword);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Dovrebbe fare login riuscito con username e password corretti")
    void testLoginSuccessful() {
        // Arrange
        String username = "testuser";
        String password = "myPassword123";
        String encodedPassword = authService.encodePassword(password);

        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(username);
        testUser.setPasswordHash(encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = authService.login(username, password);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    @DisplayName("Dovrebbe fallire il login se lo username non esiste")
    void testLoginFailedUsernameNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = authService.login("nonexistent", "anyPassword");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Dovrebbe fallire il login se la password è errata")
    void testLoginFailedWrongPassword() {
        // Arrange
        String username = "testuser";
        String correctPassword = "myPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = authService.encodePassword(correctPassword);

        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(username);
        testUser.setPasswordHash(encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = authService.login(username, wrongPassword);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("BCrypt dovrebbe generare hash diversi per la stessa password")
    void testBCryptGeneratesDifferentHashesForSamePassword() {
        // Arrange
        String rawPassword = "myPassword123";

        // Act
        String hash1 = authService.encodePassword(rawPassword);
        String hash2 = authService.encodePassword(rawPassword);

        // Assert
        assertNotEquals(hash1, hash2);
        // Ma entrambi dovrebbero verificare la stessa password
        assertTrue(authService.verifyPassword(rawPassword, hash1));
        assertTrue(authService.verifyPassword(rawPassword, hash2));
    }
}
