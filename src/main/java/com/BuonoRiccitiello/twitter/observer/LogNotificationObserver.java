package com.BuonoRiccitiello.twitter.observer;

import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementazione concreta del pattern Observer che registra le notifiche su log.
 *
 * <p><strong>Ruolo nel pattern Observer:</strong></p>
 * <p>
 * Questa classe rappresenta il ruolo di <strong>ConcreteObserver</strong> nel design pattern Observer.
 * Implementa l'interfaccia {@link MessageObserver} e fornisce una reazione concreta agli eventi:
 * registra le notifiche su log (usando SLF4J).
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Registrare su log quando un nuovo messaggio è pubblicato da un utente seguito.</li>
 *   <li>Registrare su log quando un utente seguito viene eliminato.</li>
 * </ul>
 *
 * <p><strong>Estensioni Future:</strong></p>
 * <ul>
 *   <li><strong>EmailNotificationObserver:</strong> Invierebbe email ai follower.</li>
 *   <li><strong>NotificationPersistenceObserver:</strong> Salverebbe le notifiche a DB
 *       e le mostrerebbe in una pagina HTML "Le mie notifiche".</li>
 *   <li><strong>PushNotificationObserver:</strong> Invierebbe notifiche push tramite un servizio esterno.</li>
 * </ul>
 *
 * <p><strong>Vantaggi di questa implementazione:</strong></p>
 * <ul>
 *   <li>Separazione delle responsabilità: la logica di notifica è isolata dalla logica di dominio.</li>
 *   <li>Facilità di estensione: aggiungere nuovi observer non richiede modifiche al Subject.</li>
 *   <li>Testabilità: gli observer possono essere mockati nei test.</li>
 * </ul>
 */
@Component
public class LogNotificationObserver implements MessageObserver {

    private static final Logger logger = LoggerFactory.getLogger(LogNotificationObserver.class);

    /**
     * Reagisce alla pubblicazione di un nuovo messaggio.
     *
     * <p>Registra su log che l'utente seguito ha pubblicato un messaggio.</p>
     *
     * @param message il messaggio appena pubblicato
     */
    @Override
    public void onNewMessage(Message message) {
        logger.info(
                "NOTIFICA: L'utente '{}' ha pubblicato un nuovo messaggio: \"{}\"",
                message.getAuthor().getUsername(),
                message.getContent()
        );
    }

    /**
     * Reagisce all'eliminazione di un utente seguito.
     *
     * <p>Registra su log che l'utente seguito è stato eliminato dal sistema.</p>
     *
     * @param deletedUser l'utente che è stato eliminato
     */
    @Override
    public void onUserDeleted(User deletedUser) {
        logger.info(
                "NOTIFICA: L'utente '{}' è stato eliminato dal sistema.",
                deletedUser.getUsername()
        );
    }
}
