package com.BuonoRiccitiello.twitter.factory;

import com.BuonoRiccitiello.twitter.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementazione concreta di {@link MessageChannel} per l'invio tramite instant messaging.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Questo canale gestisce l'invio dei messaggi tramite servizi di instant messaging
 * (Telegram, WhatsApp, Slack, ecc.).
 * In questo progetto, simuliamo l'invio registrando su log; in un'applicazione reale,
 * interagirebbe con le API di questi servizi.</p>
 *
 * <p><strong>Specifiche di implementazione:</strong></p>
 * <ul>
 *   <li>Il messaggio viene inviato come messaggio diretto (DM).</li>
 *   <li>Simuliamo l'invio registrando su log.</li>
 *   <li>In futuro, integreremmo una vera API IM (es. Telegram Bot API, Slack Webhook, ecc.).</li>
 * </ul>
 */
public class InstantMessagingChannel implements MessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(InstantMessagingChannel.class);

    /**
     * Invia il messaggio tramite instant messaging.
     *
     * <p>Attualmente, questo metodo registra su log il fatto che il messaggio
     * è stato inviato tramite IM. In un'applicazione reale potrebbe:</p>
     * <ul>
     *   <li>Interagire con Telegram Bot API</li>
     *   <li>Inviare messaggi tramite Slack Webhook</li>
     *   <li>Integrarsi con WhatsApp Business API</li>
     *   <li>Inviare notifiche push tramite Firebase Cloud Messaging</li>
     * </ul>
     *
     * @param message il messaggio da inviare
     */
    @Override
    public void send(Message message) {
        logger.info(
                "[INSTANT MESSAGING CHANNEL] Messaggio IM inviato: '{}' da {} via Telegram/Slack/WhatsApp (ID: {})",
                message.getContent(),
                message.getAuthor().getUsername(),
                message.getId()
        );
        // In un'app reale: 
        // TelegramBotAPI.sendMessage(chatId, message.getContent());
        // oppure SlackWebhook.postMessage(channel, message.getContent());
        // oppure WhatsAppAPI.sendMessage(phoneNumber, message.getContent());
    }
}
