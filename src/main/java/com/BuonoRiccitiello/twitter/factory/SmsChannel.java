package com.BuonoRiccitiello.twitter.factory;

import com.BuonoRiccitiello.twitter.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementazione concreta di {@link MessageChannel} per l'invio tramite SMS.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Questo canale gestisce l'invio dei messaggi tramite SMS.
 * In questo progetto, simuliamo l'invio registrando su log; in un'applicazione reale,
 * interagirebbe con un provider SMS (Twilio, AWS SNS, ecc.).</p>
 *
 * <p><strong>Specifiche di implementazione:</strong></p>
 * <ul>
 *   <li>Il messaggio deve essere abbreviato per rispettare il limite di 160 caratteri SMS.</li>
 *   <li>Simuliamo l'invio registrando su log.</li>
 *   <li>In futuro, integreremmo una vera API SMS (es. Twilio).</li>
 * </ul>
 */
public class SmsChannel implements MessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(SmsChannel.class);
    private static final int SMS_MAX_LENGTH = 160;

    /**
     * Invia il messaggio tramite SMS.
     *
     * <p>Attualmente, questo metodo:</p>
     * <ul>
     *   <li>Tronca il messaggio se necessario per rispettare il limite SMS</li>
     *   <li>Registra su log il fatto che il messaggio è stato inviato via SMS</li>
     * </ul>
     *
     * <p>In un'applicazione reale potrebbe:</p>
     * <ul>
     *   <li>Interagire con un provider SMS (Twilio, AWS SNS, ecc.)</li>
     *   <li>Tracciare lo stato di consegna</li>
     *   <li>Gestire eventuali errori di invio con retry automatici</li>
     * </ul>
     *
     * @param message il messaggio da inviare
     */
    @Override
    public void send(Message message) {
        String content = message.getContent();
        
        // Se il messaggio è più lungo del limite SMS, lo tronchiamo
        if (content.length() > SMS_MAX_LENGTH) {
            content = content.substring(0, SMS_MAX_LENGTH - 3) + "...";
        }

        logger.info(
                "[SMS CHANNEL] Messaggio SMS inviato: '{}' da {} (originale: {} caratteri)",
                content,
                message.getAuthor().getUsername(),
                message.getContent().length()
        );
        // In un'app reale: TwilioClient.sendSms(phoneNumber, content);
    }
}
