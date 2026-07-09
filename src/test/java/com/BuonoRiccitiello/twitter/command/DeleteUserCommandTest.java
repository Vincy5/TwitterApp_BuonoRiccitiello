package com.BuonoRiccitiello.twitter.command;

import com.BuonoRiccitiello.twitter.exception.UserNotFoundException;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.observer.UserSubject;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per il pattern Command - DeleteUserCommand.
 *
 * <p><strong>Test coverage:</strong></p>
 * <ul>
 *   <li>Eliminazione riuscita di un utente</li>
 *   <li>Notifica ai follower tramite Observer</li>
 *   <li>Eccezione se l'utente non esiste</li>
 *   <li>Verifica della chiamata a delete nel repository</li>
 * </ul>
 */
@DisplayName("DeleteUserCommand Pattern Tests")
class DeleteUserCommandTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSubject userSubject;

    private DeleteUserCommand deleteUserCommand;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    @DisplayName("Dovrebbe eliminare un utente dal repository")
    void testDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        deleteUserCommand = new DeleteUserCommand(1L, userRepository, userSubject);

        // Act
        deleteUserCommand.execute();

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Dovrebbe notificare i follower quando un utente viene eliminato")
    void testNotifyFollowersOnUserDeletion() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        deleteUserCommand = new DeleteUserCommand(1L, userRepository, userSubject);

        // Act
        deleteUserCommand.execute();

        // Assert
        verify(userSubject, times(1)).notifyUserDeleted(testUser);
    }

    @Test
    @DisplayName("Dovrebbe lanciare UserNotFoundException se l'utente non esiste")
    void testThrowExceptionIfUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        deleteUserCommand = new DeleteUserCommand(999L, userRepository, userSubject);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                deleteUserCommand.execute()
        );

        // Verifica che l'utente non sia stato eliminato
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Non dovrebbe notificare se l'utente non è trovato")
    void testDoNotNotifyIfUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        deleteUserCommand = new DeleteUserCommand(999L, userRepository, userSubject);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                deleteUserCommand.execute()
        );

        // Verifica che notifyUserDeleted non sia stato chiamato
        verify(userSubject, never()).notifyUserDeleted(any());
    }

    @Test
    @DisplayName("Dovrebbe mantenere l'ordine: notifica -> eliminazione")
    void testNotificationBeforeDeletion() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        deleteUserCommand = new DeleteUserCommand(1L, userRepository, userSubject);

        // Act
        deleteUserCommand.execute();

        // Assert - Verifica che entrambi i metodi siano stati chiamati
        verify(userSubject).notifyUserDeleted(testUser);
        verify(userRepository).deleteById(1L);
    }
}
