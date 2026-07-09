package com.BuonoRiccitiello.twitter.factory;

import com.BuonoRiccitiello.twitter.model.Channel;
import org.springframework.stereotype.Component;

/**
 * Factory per la creazione di istanze di {@link MessageChannel} in base al tipo di canale richiesto.
 *
 * <p><strong>Ruolo nel pattern Factory Method:</strong></p>
 * <p>
 * Questa classe rappresenta il ruolo di <strong>ConcreteCreator</strong> nel design pattern Factory Method.
 * Incapsula la logica di creazione dei channel in un unico punto, rendendo facile
 * aggiungere nuovi canali in futuro senza modificare il resto dell'applicazione.
 * </p>
 *
 * <p><strong>Principio Open/Closed:</strong></p>
 * <p>
 * La factory rispetta il principio Open/Closed: è <em>aperta all'estensione</em>
 * (aggiungere nuovi channel è facile) e <em>chiusa alla modifica</em>
 * (i client che usano la factory non devono cambiare).
 * </p>
 *
 * <p><strong>Vantaggi di questa implementazione:</strong></p>
 * <ul>
 *   <li>Incapsulamento: la logica di creazione è concentrata in un unico punto.</li>
 *   <li>Flessibilità: aggiungere un nuovo canale richiede solo di aggiungere un nuovo case nel metodo factory.</li>
 *   <li>Maintainabilità: i client non conosco i dettagli di creazione dei channel.</li>
 *   <li>Testabilità: è facile mockare la factory nei test.</li>
 * </ul>
 *
 * <p><strong>Come estendere la factory per nuovi canali:</strong></p>
 * <pre>
 *   // 1. Aggiungere un nuovo valore all'enum Channel (nel modello)
 *   public enum Channel {
 *       WEB, SMS, EMAIL, IM, PUSH_NOTIFICATION  // nuovo
 *   }
 *
 *   // 2. Creare una nuova implementazione di MessageChannel
 *   public class PushNotificationChannel implements MessageChannel { ... }
 *
 *   // 3. Aggiungere un case nel metodo createChannel()
 *   case PUSH_NOTIFICATION:
 *       return new PushNotificationChannel();
 * </pre>
 *
 * <p><strong>Gestione dei casi non previsti:</strong></p>
 * <p>Se un channel non previsto viene richiesto, la factory lancia un'eccezione
 * IllegalArgumentException. Questo è meglio che ritornare null, poiché forza
 * il client a gestire esplicitamente il caso di errore.</p>
 */
@Component
public class ChannelFactory {

    /**
     * Crea e ritorna un'istanza di {@link MessageChannel} in base al tipo richiesto.
     *
     * <p>Questo metodo incapsula la logica di creazione dei channel, nascondendola
     * dai client (il service layer, i controller, ecc.).</p>
     *
     * @param channelType il tipo di canale desiderato (WEB, SMS, EMAIL, IM)
     * @return un'istanza di MessageChannel appropriata
     * @throws IllegalArgumentException se il tipo di canale non è supportato
     */
    public MessageChannel createChannel(Channel channelType) {
        switch (channelType) {
            case WEB:
                return new WebChannel();
            case SMS:
                return new SmsChannel();
            case EMAIL:
                return new EmailChannel();
            case IM:
                return new InstantMessagingChannel();
            default:
                throw new IllegalArgumentException(
                        "Canale non supportato: " + channelType +
                        ". Canali disponibili: WEB, SMS, EMAIL, IM"
                );
        }
    }
}
