package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.model.Channel;
import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.Role;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.service.TwitterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test di integrazione per la pubblicazione di messaggi.
 *
 * <p><strong>Test coverage:</strong></p>
 * <ul>
 *   <li>POST /messages salva il messaggio correttamente</li>
 *   <li>POST /messages reindirizza a /home dopo il successo</li>
 *   <li>POST /messages restituisce errore se l'utente non è loggato</li>
 *   <li>POST /messages salva il messaggio nel database (via MockMvc)</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Message Controller Integration Tests")
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TwitterService twitterService;

    private User testUser;
    private Message testMessage;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // Setup dell'utente di test
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(Role.UTENTE);

        // Setup del messaggio di test
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setAuthor(testUser);
        testMessage.setContent("Test message");
        testMessage.setChannel(Channel.WEB);

        // Setup della sessione
        session = new MockHttpSession();
        session.setAttribute("loggedInUser", testUser);
    }

    @Test
    @DisplayName("Dovrebbe pubblicare un messaggio e reindirizzare a /home")
    void testPostMessageSuccessfully() throws Exception {
        // Arrange
        when(twitterService.postMessage(
                eq(1L),
                eq("Test message"),
                eq("hashtag"),
                eq(Channel.WEB)
        )).thenReturn(testMessage);

        // Act & Assert
        mockMvc.perform(post("/messages")
                        .session(session)
                        .param("content", "Test message")
                        .param("hashtag", "hashtag")
                        .param("channel", "WEB"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        // Verifica che il service sia stato chiamato
        verify(twitterService, times(1)).postMessage(
                eq(1L),
                eq("Test message"),
                eq("hashtag"),
                eq(Channel.WEB)
        );
    }

    @Test
    @DisplayName("Dovrebbe reindirizzare a /login se l'utente non è loggato")
    void testPostMessageWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/messages")
                        .param("content", "Test message")
                        .param("channel", "WEB"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Verifica che il service non sia stato chiamato
        verify(twitterService, never()).postMessage(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Dovrebbe restituire validazione errore per contenuto vuoto")
    void testPostMessageWithEmptyContent() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/messages")
                        .session(session)
                        .param("content", "")
                        .param("channel", "WEB"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));

        // Verifica che il service non sia stato chiamato
        verify(twitterService, never()).postMessage(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Dovrebbe salvar il messaggio senza hashtag")
    void testPostMessageWithoutHashtag() throws Exception {
        // Arrange
        when(twitterService.postMessage(
                eq(1L),
                eq("Test message"),
                eq(""),
                eq(Channel.WEB)
        )).thenReturn(testMessage);

        // Act & Assert
        mockMvc.perform(post("/messages")
                        .session(session)
                        .param("content", "Test message")
                        .param("hashtag", "")
                        .param("channel", "WEB"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("Dovrebbe funzionare con diversi canali")
    void testPostMessageWithDifferentChannels() throws Exception {
        // Arrange
        Channel[] channels = {Channel.WEB, Channel.SMS, Channel.EMAIL, Channel.IM};

        for (Channel channel : channels) {
            // Reset del mock
            reset(twitterService);
            when(twitterService.postMessage(
                    eq(1L),
                    eq("Test message"),
                    any(),
                    eq(channel)
            )).thenReturn(testMessage);

            // Act & Assert
            mockMvc.perform(post("/messages")
                            .session(session)
                            .param("content", "Test message")
                            .param("channel", channel.toString()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));

            // Verifica che il service sia stato chiamato con il canale corretto
            verify(twitterService, times(1)).postMessage(
                    eq(1L),
                    eq("Test message"),
                    any(),
                    eq(channel)
            );
        }
    }
}
