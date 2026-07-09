package com.BuonoRiccitiello.twitter.service;

import com.BuonoRiccitiello.twitter.factory.ChannelFactory;
import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.observer.LogNotificationObserver;
import com.BuonoRiccitiello.twitter.observer.UserSubject;
import com.BuonoRiccitiello.twitter.repository.HashtagRepository;
import com.BuonoRiccitiello.twitter.repository.MessageRepository;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("TwitterService - eliminazione messaggi")
class TwitterServiceDeleteMessageTest {

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
                logNotificationObserver
        );
    }

    @Test
    @DisplayName("Dovrebbe eliminare solo un messaggio dell'utente loggato")
    void shouldDeleteOwnMessage() {
        User author = new User();
        author.setId(1L);

        Message message = new Message();
        message.setId(10L);
        message.setAuthor(author);

        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

        twitterService.deleteOwnMessage(1L, 10L);

        verify(messageRepository, times(1)).delete(message);
    }

    @Test
    @DisplayName("Non dovrebbe eliminare messaggi di altri utenti")
    void shouldNotDeleteOtherUserMessage() {
        User otherAuthor = new User();
        otherAuthor.setId(2L);

        Message message = new Message();
        message.setId(10L);
        message.setAuthor(otherAuthor);

        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

        assertThrows(IllegalArgumentException.class, () ->
                twitterService.deleteOwnMessage(1L, 10L)
        );

        verify(messageRepository, never()).delete(any(Message.class));
    }
}
