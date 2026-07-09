package com.BuonoRiccitiello.twitter.factory;

import com.BuonoRiccitiello.twitter.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementazione concreta di {@link MessageChannel} per l'invio tramite email.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Questo canale gestisce l'invio dei messaggi tramite email.
 * In questo progetto, simuliamo l'invio registrando su log; in un'applicazione reale,
 * interagirebbe con un servizio SMTP o un provider di email (SendGrid, AWS SES, ecc.).</p>
 *
 * <p><strong>Specifiche di implementazione:</strong></p>
 * <ul>
 *   <li>Il messaggio viene formattato con un subject e un body HTML.</li>
 *   <li>Simuliamo l'invio registrando su log.</li>
 *   <li>In futuro, integreremmo una vera API email (es. SendGrid, AWS SES).</li>
 * </ul>
 */
public class EmailChannel implements MessageChannel {

    private static final Logger logger = LoggerFactory.getLogger(EmailChannel.class);

    /**
     * Invia il messaggio tramite email.
     *
     * <p>Attualmente, questo metodo:</p>
     * <ul>
     *   <li>Crea un subject basato sul messaggio</li>
     *   <li>Registra su log il fatto che il messaggio è stato inviato via email</li>
     * </ul>
     *
     * <p>In un'applicazione reale potrebbe:</p>
     * <ul>
     *   <li>Interagire con un provider SMTP (Java Mail, SendGrid, AWS SES, ecc.)</li>
     *   <li>Formattare un HTML template accattivante</li>
     *   <li>Tracciare le aperture e i clic tramite tracking pixel</li>
     *   <li>Gestire eventuali bounce e unsubscribe</li>
     * </ul>
     *
     * @param message il messaggio da inviare
     */
    @Override
    public void send(Message message) {
        String subject = "Nuovo messaggio da " + message.getAuthor().getUsername();
        String htmlContent = "<h2>" + subject + "</h2><p>" + message.getContent() + "</p>";

        logger.info(
                "[EMAIL CHANNEL] Email inviata con subject: '{}' - Contenuto: '{}' - Mittente: {}",
                subject,
                message.getContent(),
                message.getAuthor().getUsername()
        );
        // In un'app reale: 
        // JavaMailSender.send(new SimpleMailMessage(...));
        // oppure SendGridClient.sendEmail(recipientEmail, subject, htmlContent);
    }
}
