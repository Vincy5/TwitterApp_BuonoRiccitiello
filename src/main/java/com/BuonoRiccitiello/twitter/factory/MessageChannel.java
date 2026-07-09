package com.BuonoRiccitiello.twitter.factory;

import com.BuonoRiccitiello.twitter.model.Message;

/**
 * Interfaccia che definisce il contratto per i canali di invio dei messaggi.
 *
 * <p><strong>Ruolo nel pattern Factory Method:</strong></p>
 * <p>
 * Questa interfaccia rappresenta il ruolo di <strong>Product</strong> nel design pattern Factory Method.
 * Ogni implementazione (WebChannel, SmsChannel, EmailChannel, InstantMessagingChannel)
 * fornisce un modo concreto di inviare un messaggio attraverso un mezzo diverso.
 * </p>
 *
 * <p><strong>Implementazioni disponibili:</strong></p>
 * <ul>
 *   <li>{@link WebChannel} - Invia il messaggio tramite interfaccia web</li>
 *   <li>{@link SmsChannel} - Invia il messaggio tramite SMS</li>
 *   <li>{@link EmailChannel} - Invia il messaggio tramite email</li>
 *   <li>{@link InstantMessagingChannel} - Invia il messaggio tramite instant messaging</li>
 * </ul>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Ogni implementazione di MessageChannel si occupa di gestire i dettagli specifici
 * del canale di invio, lasciando il resto dell'applicazione ignaro dei dettagli tecnici.</p>
 */
public interface MessageChannel {

    /**
     * Invia il messaggio attraverso il canale specifico.
     *
     * <p>Questo metodo gestisce tutta la logica di invio specifica del canale:
     * formattazione del messaggio, validazioni, interazione con servizi esterni, logging, ecc.</p>
     *
     * @param message il messaggio da inviare
     */
    void send(Message message);
}
