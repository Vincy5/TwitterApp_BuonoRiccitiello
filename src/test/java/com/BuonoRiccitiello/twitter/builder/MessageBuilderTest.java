package com.BuonoRiccitiello.twitter.builder;

import com.BuonoRiccitiello.twitter.exception.MessageTooLongException;
import com.BuonoRiccitiello.twitter.model.Channel;
import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per il pattern Builder applicato ai messaggi.
 *
 * <p><strong>Test coverage:</strong></p>
 * <ul>
 *   <li>Costruzione valida di un messaggio</li>
 *   <li>Validazione della lunghezza (max 140 caratteri)</li>
 *   <li>Eccezione per contenuto troppo lungo</li>
 *   <li>Validazione dei campi obbligatori</li>
 * </ul>
 */
@DisplayName("MessageBuilder Unit Tests")
class MessageBuilderTest {

    private MessageBuilder messageBuilder;
    private User testAuthor;

    @BeforeEach
    void setUp() {
        messageBuilder = new MessageBuilder();
        testAuthor = new User();
        testAuthor.setId(1L);
        testAuthor.setUsername("testuser");
    }

    @Test
    @DisplayName("Dovrebbe costruire un messaggio valido con 140 caratteri")
    void testBuildMessageWithMaxLength() {
        // Arrange
        String maxContent = "a".repeat(140);

        // Act
        Message message = messageBuilder
                .withAuthor(testAuthor)
                .withContent(maxContent)
                .withChannel(Channel.WEB)
                .build();

        // Assert
        assertNotNull(message);
        assertEquals(testAuthor, message.getAuthor());
        assertEquals(maxContent, message.getContent());
        assertEquals(Channel.WEB, message.getChannel());
    }

    @Test
    @DisplayName("Dovrebbe costruire un messaggio valido con meno di 140 caratteri")
    void testBuildMessageWithShortContent() {
        // Arrange
        String shortContent = "Ciao a tutti!";

        // Act
        Message message = messageBuilder
                .withAuthor(testAuthor)
                .withContent(shortContent)
                .withChannel(Channel.SMS)
                .build();

        // Assert
        assertNotNull(message);
        assertEquals(shortContent, message.getContent());
    }

    @Test
    @DisplayName("Dovrebbe lanciare MessageTooLongException per contenuto > 140 caratteri")
    void testBuildMessageWithTooLongContent() {
        // Arrange
        String tooLongContent = "a".repeat(141);

        // Act & Assert
        MessageTooLongException exception = assertThrows(MessageTooLongException.class, () ->
                messageBuilder
                        .withAuthor(testAuthor)
                        .withContent(tooLongContent)
                        .withChannel(Channel.WEB)
                        .build()
        );

        assertTrue(exception.getMessage().contains("140"));
    }

    @Test
    @DisplayName("Dovrebbe lanciare IllegalArgumentException se autore manca")
    void testBuildMessageWithoutAuthor() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                messageBuilder
                        .withContent("Test message")
                        .withChannel(Channel.WEB)
                        .build()
        );
    }

    @Test
    @DisplayName("Dovrebbe lanciare IllegalArgumentException se contenuto manca")
    void testBuildMessageWithoutContent() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                messageBuilder
                        .withAuthor(testAuthor)
                        .withChannel(Channel.WEB)
                        .build()
        );
    }

    @Test
    @DisplayName("Dovrebbe lanciare IllegalArgumentException se canale manca")
    void testBuildMessageWithoutChannel() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                messageBuilder
                        .withAuthor(testAuthor)
                        .withContent("Test message")
                        .build()
        );
    }

    @Test
    @DisplayName("Dovrebbe permettere hashtag opzionale")
    void testBuildMessageWithOptionalHashtag() {
        // Arrange & Act
        Message messageWithoutHashtag = messageBuilder
                .withAuthor(testAuthor)
                .withContent("Test")
                .withChannel(Channel.WEB)
                .build();

        // Assert
        assertNull(messageWithoutHashtag.getHashtag());
    }
}
