package com.BuonoRiccitiello.twitter.factory;

import com.BuonoRiccitiello.twitter.model.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per il pattern Factory Method.
 *
 * <p><strong>Test coverage:</strong></p>
 * <ul>
 *   <li>Creazione di WebChannel</li>
 *   <li>Creazione di SmsChannel</li>
 *   <li>Creazione di EmailChannel</li>
 *   <li>Creazione di InstantMessagingChannel</li>
 *   <li>Eccezione per canale non supportato</li>
 * </ul>
 */
@DisplayName("ChannelFactory Factory Method Tests")
class ChannelFactoryTest {

    private ChannelFactory channelFactory;

    @BeforeEach
    void setUp() {
        channelFactory = new ChannelFactory();
    }

    @Test
    @DisplayName("Dovrebbe creare un WebChannel")
    void testCreateWebChannel() {
        // Act
        MessageChannel channel = channelFactory.createChannel(Channel.WEB);

        // Assert
        assertNotNull(channel);
        assertInstanceOf(WebChannel.class, channel);
    }

    @Test
    @DisplayName("Dovrebbe creare un SmsChannel")
    void testCreateSmsChannel() {
        // Act
        MessageChannel channel = channelFactory.createChannel(Channel.SMS);

        // Assert
        assertNotNull(channel);
        assertInstanceOf(SmsChannel.class, channel);
    }

    @Test
    @DisplayName("Dovrebbe creare un EmailChannel")
    void testCreateEmailChannel() {
        // Act
        MessageChannel channel = channelFactory.createChannel(Channel.EMAIL);

        // Assert
        assertNotNull(channel);
        assertInstanceOf(EmailChannel.class, channel);
    }

    @Test
    @DisplayName("Dovrebbe creare un InstantMessagingChannel")
    void testCreateInstantMessagingChannel() {
        // Act
        MessageChannel channel = channelFactory.createChannel(Channel.IM);

        // Assert
        assertNotNull(channel);
        assertInstanceOf(InstantMessagingChannel.class, channel);
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione per canale null")
    void testCreateChannelWithNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                channelFactory.createChannel(null)
        );
    }

    @Test
    @DisplayName("Factory dovrebbe rispettare Open/Closed Principle")
    void testFactorySupportsAllChannelTypes() {
        // Arrange
        Channel[] allChannels = Channel.values();

        // Act & Assert
        for (Channel channel : allChannels) {
            MessageChannel messageChannel = channelFactory.createChannel(channel);
            assertNotNull(messageChannel);
            assertTrue(messageChannel instanceof MessageChannel);
        }
    }
}
