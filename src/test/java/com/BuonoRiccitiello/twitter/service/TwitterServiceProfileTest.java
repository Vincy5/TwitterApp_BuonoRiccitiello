package com.BuonoRiccitiello.twitter.service;

import com.BuonoRiccitiello.twitter.observer.NotificationPersistenceObserver;
import com.BuonoRiccitiello.twitter.factory.ChannelFactory;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.observer.LogNotificationObserver;
import com.BuonoRiccitiello.twitter.observer.UserSubject;
import com.BuonoRiccitiello.twitter.repository.HashtagRepository;
import com.BuonoRiccitiello.twitter.repository.MessageRepository;
import com.BuonoRiccitiello.twitter.repository.NotificationRepository;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import com.BuonoRiccitiello.twitter.storage.AvatarStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TwitterService - profilo e sicurezza")
class TwitterServiceProfileTest {

    private TwitterService twitterService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ChannelFactory channelFactory;

    @Mock
    private UserSubject userSubject;

    @Mock
    private LogNotificationObserver logNotificationObserver;

    @Mock
    private AvatarStorage avatarStorage;

    @Mock
    private NotificationPersistenceObserver notificationPersistenceObserver;

    @Mock
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
            twitterService = new TwitterService(
            userRepository,
            messageRepository,
            hashtagRepository,
            authService,
            channelFactory,
            userSubject,
            logNotificationObserver,
            notificationPersistenceObserver,
            avatarStorage,
            notificationRepository
        );
    }

    @Test
    @DisplayName("Dovrebbe aggiornare la password se quella attuale è corretta")
    void shouldChangePasswordWhenCurrentPasswordIsValid() {
        User user = new User();
        user.setId(1L);
        user.setUsername("vbuono");
        user.setPasswordHash("oldHash");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authService.verifyPassword("oldPassword", "oldHash")).thenReturn(true);
        when(authService.encodePassword("newPassword")).thenReturn("newHash");

        twitterService.changePassword(1L, "oldPassword", "newPassword", "newPassword");

        verify(userRepository).save(user);
        verify(authService).encodePassword("newPassword");
    }

    @Test
    @DisplayName("Non dovrebbe aggiornare la password se quella attuale è errata")
    void shouldNotChangePasswordWhenCurrentPasswordIsInvalid() {
        User user = new User();
        user.setId(1L);
        user.setPasswordHash("oldHash");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authService.verifyPassword("wrongPassword", "oldHash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                twitterService.changePassword(1L, "wrongPassword", "newPassword", "newPassword")
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
