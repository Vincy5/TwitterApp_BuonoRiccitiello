package com.BuonoRiccitiello.twitter.observer;

import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer che invia (simula) una comunicazione a tutti i follower di un utente
 * quando quell'utente viene eliminato dall'amministratore.
 *
 * <p>In questa implementazione, simuliamo l'invio registrando su log una riga per
 * ogni follower. In un'applicazione reale si potrebbe integrare un servizio email
 * o di notifiche push.</p>
 */
@Component
public class FollowersNotificationObserver implements MessageObserver {

    private static final Logger logger = LoggerFactory.getLogger(FollowersNotificationObserver.class);

    @Override
    public void onNewMessage(Message message) {
        // Non usato per ora. Potremmo inviare notifiche quando un followed pubblica messaggi.
    }

    @Override
    public void onUserDeleted(User deletedUser) {
        if (deletedUser == null) return;

        for (User follower : deletedUser.getFollowers()) {
            // Simuliamo l'invio di una comunicazione al follower
            logger.info("COMUNICAZIONE A FOLLOWER: inviata a '{}' <{}> -> L'utente '{}' è stato eliminato dal sistema",
                    follower.getUsername(), follower.getEmail(), deletedUser.getUsername());
        }
    }
}

