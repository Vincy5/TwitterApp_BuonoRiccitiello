package com.BuonoRiccitiello.twitter.builder;

import com.BuonoRiccitiello.twitter.exception.MessageTooLongException;
import com.BuonoRiccitiello.twitter.model.Channel;
import com.BuonoRiccitiello.twitter.model.Hashtag;
import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;

/**
 * Implementazione del design pattern Builder per la creazione di entità Message.
 *
 * <p><strong>Motivazione della scelta del Builder:</strong></p>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> Separa la logica di creazione
 *       e validazione di un Message dalla sua logica di dominio.</li>
 *   <li><strong>Leggibilità:</strong> I metodi fluenti con nomi espliciti (withAuthor,
 *       withContent, ...) rendono il codice autoesplicativo e facile da leggere.</li>
 *   <li><strong>Validazione centralizzata:</strong> Tutte le regole di validazione (es. lunghezza
 *       massima 140 caratteri) sono concentrate nel metodo build(), evitando duplicazione
 *       nei costruttori o nei metodi di setter.</li>
 *   <li><strong>Flessibilità:</strong> Consente di creare Message con diversi sottoinsiemi di attributi
 *       senza dover esporre costruttori sovraccarichi e poco gestibili.</li>
 * </ul>
 *
 * <p><strong>Utilizzo:</strong></p>
 * <pre>
 *   Message msg = new MessageBuilder()
 *       .withAuthor(user)
 *       .withContent("Ciao a tutti!")
 *       .withHashtag(hashtag)
 *       .withChannel(Channel.WEB)
 *       .build(); // Valida e restituisce il messaggio
 * </pre>
 */
public class MessageBuilder {

    private User author;
    private String content;
    private Hashtag hashtag;
    private Channel channel;

    /**
     * Imposta l'autore del messaggio (obbligatorio).
     *
     * @param author l'utente autore del messaggio
     * @return this (per concatenamento fluente)
     */
    public MessageBuilder withAuthor(User author) {
        this.author = author;
        return this;
    }

    /**
     * Imposta il contenuto del messaggio (obbligatorio).
     *
     * @param content il testo del messaggio (max 140 caratteri)
     * @return this (per concatenamento fluente)
     */
    public MessageBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Imposta l'hashtag associato al messaggio (opzionale).
     *
     * @param hashtag l'hashtag (può essere null)
     * @return this (per concatenamento fluente)
     */
    public MessageBuilder withHashtag(Hashtag hashtag) {
        this.hashtag = hashtag;
        return this;
    }

    /**
     * Imposta il canale di invio del messaggio (obbligatorio).
     *
     * @param channel il canale (WEB, MOBILE, API, ecc.)
     * @return this (per concatenamento fluente)
     */
    public MessageBuilder withChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    /**
     * Valida il messaggio e lo restituisce pronto per essere persistito.
     *
     * <p>Controlli effettuati:</p>
     * <ul>
     *   <li>Il contenuto non è null e non è vuoto.</li>
     *   <li>Il contenuto non supera 140 caratteri.</li>
     *   <li>L'autore è specificato.</li>
     *   <li>Il canale è specificato.</li>
     * </ul>
     *
     * @return l'entità Message validata e pronta per il salvataggio
     * @throws MessageTooLongException se il contenuto supera 140 caratteri
     * @throws IllegalArgumentException se mancano campi obbligatori
     */
    public Message build() {
        // Validazione della lunghezza del messaggio
        if (content != null && content.length() > 140) {
            throw new MessageTooLongException(
                    "Il messaggio non può superare 140 caratteri. "
                    + "Lunghezza attuale: " + content.length()
            );
        }

        // Validazione dei campi obbligatori
        if (author == null) {
            throw new IllegalArgumentException("L'autore del messaggio è obbligatorio.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Il contenuto del messaggio è obbligatorio e non può essere vuoto.");
        }

        if (channel == null) {
            throw new IllegalArgumentException("Il canale di invio è obbligatorio.");
        }

        // Creazione dell'entità Message
        Message message = new Message();
        message.setAuthor(author);
        message.setContent(content);
        message.setHashtag(hashtag); // può essere null
        message.setChannel(channel);

        return message;
    }
}
