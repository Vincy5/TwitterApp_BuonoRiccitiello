package com.BuonoRiccitiello.twitter.observer;

import com.BuonoRiccitiello.twitter.model.Notification;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.repository.NotificationRepository;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer che persiste le notifiche per ogni utente destinatario.
 * Viene usato per creare una notifica per ogni follower quando un utente viene eliminato.
 */
@Component
public class NotificationPersistenceObserver implements MessageObserver {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPersistenceObserver.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationPersistenceObserver(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void onNewMessage(Message message) {
        // Non utilizzato in questa implementazione, ma potrebbe creare notifiche per nuovi messaggi
    }

    @Override
    public void onUserDeleted(User deletedUser) {
        if (deletedUser == null || deletedUser.getFollowers() == null) {
            return;
        }

        String deletedUsername = deletedUser.getUsername();

        for (User follower : deletedUser.getFollowers()) {
            if (follower == null || follower.getId() == null) {
                continue;
            }

            String text = String.format(
                    "L'utente '%s' che seguivi è stato eliminato dal sistema.",
                    deletedUsername
            );

            User recipientRef = userRepository.getReferenceById(follower.getId());

            Notification notification = new Notification();
            notification.setRecipient(recipientRef);
            notification.setMessage(text);
            notification.setRead(false);

            notificationRepository.save(notification);

            logger.info(
                    "Notifica salvata per '{}' perché seguiva '{}'",
                    follower.getUsername(),
                    deletedUsername
            );
        }
    }
}

