package com.BuonoRiccitiello.twitter.observer;

import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta il ruolo di <strong>Subject</strong> nel design pattern Observer.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Mantenere la lista degli observer (i follower di un utente).</li>
 *   <li>Fornire metodi per registrare e deregistrare observer.</li>
 *   <li>Notificare gli observer quando accadono eventi rilevanti
 *       (nuovo messaggio pubblicato, utente eliminato).</li>
 * </ul>
 *
 * <p><strong>Perché gestito come @Component Spring:</strong></p>
 * <p>
 * Anche se implementiamo il pattern Observer "a mano", lo componentiamo come bean Spring
 * per permettere l'injection nei service layer e nel controller. Questo non nasconde il pattern:
 * manteniamo esplicitamente la lista di observer e la logica di notifica, senza usare
 * ApplicationEventPublisher di Spring, che astrairebbe il pattern.
 * </p>
 *
 * <p><strong>Nota importante:</strong></p>
 * <p>
 * Non usiamo ApplicationEventPublisher di Spring perché vogliamo dimostrare esplicitamente
 * la comprensione del pattern Observer. Sebbene Spring Event sia una validissima alternativa
 * per applicazioni enterprise, qui implementiamo il pattern "puro" per chiarezza didattica.
 * </p>
 *
 * <p><strong>Utilizzo:</strong></p>
 * <pre>
 *   // Nel service layer:
 *   userSubject.notifyNewMessage(message); // Tutti gli observer ricevono la notifica
 *   userSubject.notifyUserDeleted(user);   // Utente eliminato, notifica ai follower
 * </pre>
 */
@Component
public class UserSubject {

    /**
     * Lista degli observer (i follower dell'utente).
     * In un'implementazione più sofisticata, potrebbe essere organizzata per utente.
     */
    private final List<MessageObserver> observers = new ArrayList<>();

    /**
     * Registra un nuovo observer nella lista.
     *
     * <p>Tipicamente, questo sarà un object che rappresenta un follower di un utente
     * e vuole essere notificato quando l'utente pubblica messaggi o viene eliminato.</p>
     *
     * @param observer l'observer da registrare
     */
    public void attach(MessageObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Deregistra un observer dalla lista.
     *
     * @param observer l'observer da rimuovere
     */
    public void detach(MessageObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifica tutti gli observer registrati di un nuovo messaggio pubblicato.
     *
     * <p>Questo metodo viene chiamato dal service layer quando un messaggio è stato
     * salvato a DB. Tutti gli observer (i follower dell'autore) ricevono la notifica.</p>
     *
     * @param message il messaggio appena pubblicato
     */
    public void notifyNewMessage(Message message) {
        for (MessageObserver observer : observers) {
            observer.onNewMessage(message);
        }
    }

    /**
     * Notifica tutti gli observer registrati che un utente è stato eliminato.
     *
     * <p>Questo metodo viene chiamato dal service layer quando un utente è stato
     * cancellato dal database. Tutti gli observer (i follower dell'utente eliminato)
     * ricevono la notifica.</p>
     *
     * @param deletedUser l'utente che è stato eliminato
     */
    public void notifyUserDeleted(User deletedUser) {
        for (MessageObserver observer : observers) {
            observer.onUserDeleted(deletedUser);
        }
    }

    /**
     * Restituisce il numero di observer attualmente registrati.
     *
     * <p>Utile per testing e debugging.</p>
     *
     * @return il numero di observer
     */
    public int getObserversCount() {
        return observers.size();
    }

    /**
     * Elimina tutti gli observer registrati.
     *
     * <p>Utile per pulizia in scenari di test o reset del sistema.</p>
     */
    public void clearObservers() {
        observers.clear();
    }
}
