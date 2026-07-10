package com.BuonoRiccitiello.twitter.command;

import com.BuonoRiccitiello.twitter.exception.UserNotFoundException;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.observer.UserSubject;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import com.BuonoRiccitiello.twitter.repository.MessageRepository;
import com.BuonoRiccitiello.twitter.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementazione concreta del pattern Command che elimina un utente dal sistema.
 *
 * <p><strong>Ruolo nel pattern Command:</strong></p>
 * <p>
 * Questa classe rappresenta il ruolo di <strong>ConcreteCommand</strong> nel design pattern Command.
 * Incapsula l'operazione di eliminazione di un utente, incluse tutte le ripercussioni:
 * </p>
 * <ul>
 *   <li>Eliminazione dell'utente dal database.</li>
 *   <li>Notifica ai follower tramite il pattern Observer.</li>
 *   <li>Logging dell'operazione per audit trail.</li>
 * </ul>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Verificare che l'utente esista; se no, lanciare UserNotFoundException.</li>
 *   <li>Eliminare l'utente dal repository.</li>
 *   <li>Notificare tutti i follower tramite UserSubject.</li>
 *   <li>Registrare l'operazione su log.</li>
 * </ul>
 *
 * <p><strong>Dependency Injection:</strong></p>
 * <p>Le dipendenze (UserRepository e UserSubject) sono passate nel costruttore,
 * seguendo il principio di Inversion of Control. Questo facilita il testing
 * e rende il comando facilmente mockabile.</p>
 */
public class DeleteUserCommand implements AdminCommand {

    private static final Logger logger = LoggerFactory.getLogger(DeleteUserCommand.class);

    private final Long userId;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final UserSubject userSubject;
    private final NotificationRepository notificationRepository;

    /**
     * Costruttore del comando.
     *
     * @param userId l'ID dell'utente da eliminare
     * @param userRepository il repository per accedere ai dati degli utenti
     * @param userSubject il subject per notificare gli observer
     */
    public DeleteUserCommand(
                Long userId,
                UserRepository userRepository,
                MessageRepository messageRepository,
                NotificationRepository notificationRepository,
                UserSubject userSubject
        ) {
        this.userId = userId;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.userSubject = userSubject;
}

    /**
     * Esegue l'eliminazione dell'utente.
     *
     * <p>Passaggi:</p>
     * <ol>
     *   <li>Ricerca l'utente nel database.</li>
     *   <li>Se non trovato, lancia UserNotFoundException.</li>
     *   <li>Notifica tutti i follower tramite UserSubject.</li>
     *   <li>Elimina l'utente dal database.</li>
     *   <li>Registra l'operazione su log.</li>
     * </ol>
     *
     * @throws UserNotFoundException se l'utente con l'ID specificato non esiste
     */
    @Override
        public void execute() throws UserNotFoundException {
        logger.info("Inizio esecuzione comando: DeleteUserCommand per utente ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Utente con ID " + userId + " non trovato nel sistema."
                ));

        String username = user.getUsername();

        logger.info(
                "L'utente '{}' ha {} follower da notificare.",
                username,
                user.getFollowers().size()
        );

        /*
        * 1. Prima notifico i follower.
        * In questo momento le relazioni followers/following esistono ancora,
        * quindi l'Observer riesce a capire chi deve ricevere la notifica.
        */
        userSubject.notifyUserDeleted(user);

        /*
        * 2. Elimino i messaggi pubblicati dall'utente.
        * Serve perché Message ha author_id collegato a User.
        */
        messageRepository.deleteByAuthor_Id(userId);

        /*
        * 3. Elimino le notifiche ricevute dall'utente che sto eliminando.
        * Serve perché Notification ha recipient_id collegato a User.
        */
        notificationRepository.deleteByRecipient_Id(userId);

        /*
        * 4. Elimino tutte le relazioni follower/following dalla tabella ponte.
        */
        userRepository.deleteAllFollowRelationsForUser(userId);

        /*
        * 5. Solo alla fine elimino l'utente.
        */
        userRepository.deleteById(userId);

        logger.info(
                "Utente '{}' (ID={}) eliminato con successo dal sistema",
                username,
                userId
        );
        }
}
