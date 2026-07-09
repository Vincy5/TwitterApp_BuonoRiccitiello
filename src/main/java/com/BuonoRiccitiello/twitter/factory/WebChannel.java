package com.BuonoRiccitiello.twitter.factory;

import com.BuonoRiccitiello.twitter.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementazione concreta di {@link MessageChannel} per l'invio tramite interfaccia web.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Questo canale gestisce l'invio dei messaggi tramite l'interfaccia web.
 * In questo progetto, simuliamo l'invio registrando su log; in un'applicazione reale,
 * potrebbe aggiornare una dashboard in tempo reale (via WebSocket) o salvare una notifica nel DB.</p>
 *
 * <p><strong>Specifiche di implementazione:</strong></p>
 * <ul>
 *   <li>Il messaggio è già stato persistito nel database dal service layer.</li>
 *   <li>Questo canale registra su log che il messaggio è stato trasmesso.</li>
 *   <li>In futuro, potrebbe inviare il messaggio tramite WebSocket ai client connessi.</li>
 * </ul>
 */
public class WebChannel implements MessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebChannel.class);

    /**
     * Invia il messaggio tramite il canale web.
     *
     * <p>Attualmente, questo metodo registra su log il fatto che il messaggio
     * è stato inviato tramite web. In un'applicazione reale potrebbe:</p>
     * <ul>
     *   <li>Notificare i client connessi via WebSocket</li>
     *   <li>Salvare il messaggio nella feed dell'utente</li>
     *   <li>Scatenare notifiche in-app</li>
     * </ul>
     *
     * @param message il messaggio da inviare
     */
    @Override
    public void send(Message message) {
        logger.info(
                "[WEB CHANNEL] Messaggio inviato via web: '{}' da {} (ID: {})",
                message.getContent(),
                message.getAuthor().getUsername(),
                message.getId()
        );
        // In un'app reale: notifyWebSocketClients(message);
    }
}
