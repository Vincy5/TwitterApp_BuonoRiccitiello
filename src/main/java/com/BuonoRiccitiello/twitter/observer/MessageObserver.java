package com.BuonoRiccitiello.twitter.observer;

import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;

/**
 * Interfaccia che definisce il contratto per gli observer del sistema di notifiche.
 *
 * <p><strong>Ruolo nel pattern Observer:</strong></p>
 * <p>
 * Questa interfaccia rappresenta il ruolo di <strong>Observer</strong> nel design pattern Observer.
 * Un observer è un oggetto che vuole essere notificato quando accadono certi eventi
 * (ad esempio, quando un utente che segue pubblica un messaggio o quando viene eliminato).
 * </p>
 *
 * <p><strong>Implementazioni:</strong></p>
 * <ul>
 *   <li>{@link LogNotificationObserver} - Registra le notifiche su log</li>
 *   <li>EmailNotificationObserver - (Futura estensione) Invierebbe email agli utenti</li>
 *   <li>NotificationPersistenceObserver - (Futura estensione) Salverebbe a DB</li>
 * </ul>
 */
public interface MessageObserver {

    /**
     * Notificazione di un nuovo messaggio pubblicato da un utente seguito.
     *
     * <p>Questo metodo viene invocato quando un utente seguito pubblica un nuovo messaggio.
     * L'observer decide come reagire: registrare su log, inviare email, salvare a DB, ecc.</p>
     *
     * @param message il messaggio appena pubblicato
     */
    void onNewMessage(Message message);

    /**
     * Notificazione dell'eliminazione di un utente seguito.
     *
     * <p>Questo metodo viene invocato quando un utente seguito viene eliminato dal sistema.
     * L'observer decide come reagire: registrare su log, inviare notifica, pulire i dati associati, ecc.</p>
     *
     * @param deletedUser l'utente che è stato eliminato
     */
    void onUserDeleted(User deletedUser);
}
