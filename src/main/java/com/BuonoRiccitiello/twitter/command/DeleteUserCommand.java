package com.BuonoRiccitiello.twitter.command;

import com.BuonoRiccitiello.twitter.exception.UserNotFoundException;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.observer.UserSubject;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import com.BuonoRiccitiello.twitter.repository.MessageRepository;
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

    /**
     * Costruttore del comando.
     *
     * @param userId l'ID dell'utente da eliminare
     * @param userRepository il repository per accedere ai dati degli utenti
     * @param userSubject il subject per notificare gli observer
     */
    public DeleteUserCommand(Long userId, UserRepository userRepository, MessageRepository messageRepository, UserSubject userSubject) {
        this.userId = userId;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
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

        // 1. Ricerca l'utente
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Utente con ID " + userId + " non trovato nel sistema."
                ));

        // 2. Rimuove i messaggi dell'utente per evitare vincoli referenziali
        logger.debug("Rimozione messaggi dell'utente ID={} prima dell'eliminazione", userId);
        messageRepository.deleteByAuthor_Id(userId);

        // 3. Notifica i follower tramite Observer pattern
        logger.debug("Notificazione in corso ai {} follower dell'utente '{}'",
                user.getFollowers().size(), user.getUsername());
        userSubject.notifyUserDeleted(user);

        // 4. Prima dell'eliminazione pulizia dalle relazioni ManyToMany

        user.getFollowers().forEach(follower -> follower.getFollowing().remove(user));
        user.getFollowing().forEach(followed -> followed.getFollowers().remove(user));

        user.getFollowers().clear();
        user.getFollowing().clear();

        userRepository.delete(user);

        // 4. Log dell'operazione completata
        logger.info("Utente '{}' (ID={}) eliminato con successo dal sistema", 
                user.getUsername(), userId);
    }
}
