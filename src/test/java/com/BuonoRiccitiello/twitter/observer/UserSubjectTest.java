package com.BuonoRiccitiello.twitter.observer;

import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per il pattern Observer.
 *
 * <p><strong>Test coverage:</strong></p>
 * <ul>
 *   <li>Registrazione di un observer</li>
 *   <li>Deregistrazione di un observer</li>
 *   <li>Notifica di tutti i follower su nuovo messaggio</li>
 *   <li>Notifica di tutti i follower su eliminazione utente</li>
 *   <li>Conteggio degli observer registrati</li>
 * </ul>
 */
@DisplayName("UserSubject Observer Pattern Tests")
class UserSubjectTest {

    private UserSubject userSubject;

    @Mock
    private MessageObserver observer1;

    @Mock
    private MessageObserver observer2;

    @Mock
    private Message testMessage;

    @Mock
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userSubject = new UserSubject();
    }

    @Test
    @DisplayName("Dovrebbe registrare un observer")
    void testAttachObserver() {
        // Act
        userSubject.attach(observer1);

        // Assert
        assertEquals(1, userSubject.getObserversCount());
    }

    @Test
    @DisplayName("Dovrebbe deregistrare un observer")
    void testDetachObserver() {
        // Arrange
        userSubject.attach(observer1);
        userSubject.attach(observer2);
        assertEquals(2, userSubject.getObserversCount());

        // Act
        userSubject.detach(observer1);

        // Assert
        assertEquals(1, userSubject.getObserversCount());
    }

    @Test
    @DisplayName("Dovrebbe notificare tutti gli observer su nuovo messaggio")
    void testNotifyAllObserversOnNewMessage() {
        // Arrange
        userSubject.attach(observer1);
        userSubject.attach(observer2);

        // Act
        userSubject.notifyNewMessage(testMessage);

        // Assert
        verify(observer1, times(1)).onNewMessage(testMessage);
        verify(observer2, times(1)).onNewMessage(testMessage);
    }

    @Test
    @DisplayName("Dovrebbe notificare tutti gli observer su eliminazione utente")
    void testNotifyAllObserversOnUserDeleted() {
        // Arrange
        userSubject.attach(observer1);
        userSubject.attach(observer2);

        // Act
        userSubject.notifyUserDeleted(testUser);

        // Assert
        verify(observer1, times(1)).onUserDeleted(testUser);
        verify(observer2, times(1)).onUserDeleted(testUser);
    }

    @Test
    @DisplayName("Non dovrebbe notificare observer non registrati")
    void testDoNotNotifyDetachedObservers() {
        // Arrange
        userSubject.attach(observer1);
        userSubject.attach(observer2);
        userSubject.detach(observer1);

        // Act
        userSubject.notifyNewMessage(testMessage);

        // Assert
        verify(observer1, never()).onNewMessage(testMessage);
        verify(observer2, times(1)).onNewMessage(testMessage);
    }

    @Test
    @DisplayName("Dovrebbe evitare duplicati durante la registrazione")
    void testPreventDuplicateObservers() {
        // Arrange
        userSubject.attach(observer1);

        // Act
        userSubject.attach(observer1); // Tenta di registrare di nuovo lo stesso observer

        // Assert
        assertEquals(1, userSubject.getObserversCount());
    }

    @Test
    @DisplayName("Dovrebbe clearare tutti gli observer")
    void testClearAllObservers() {
        // Arrange
        userSubject.attach(observer1);
        userSubject.attach(observer2);
        assertEquals(2, userSubject.getObserversCount());

        // Act
        userSubject.clearObservers();

        // Assert
        assertEquals(0, userSubject.getObserversCount());
    }
}
