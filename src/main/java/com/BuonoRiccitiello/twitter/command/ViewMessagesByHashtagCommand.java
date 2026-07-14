package com.BuonoRiccitiello.twitter.command;

import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementazione concreta del pattern Command che recupera i messaggi raggruppati per hashtag.
 *
 * <p><strong>Ruolo nel pattern Command:</strong></p>
 * <p>
 * Questa classe rappresenta il ruolo di <strong>ConcreteCommand</strong> nel design pattern Command.
 * Incapsula l'operazione di ricerca e raggruppamento dei messaggi per hashtag.
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Ricercare i messaggi associati a un hashtag specifico.</li>
 *   <li>Raggruppare i messaggi per hashtag (utile se in futuro si vuole estendere
 *       per ricercare su più hashtag contemporaneamente).</li>
 *   <li>Registrare l'operazione su log.</li>
 *   <li>Restituire i risultati al CommandInvoker.</li>
 * </ul>
 *
 * <p><strong>Dependency Injection:</strong></p>
 * <p>Il MessageRepository è passato nel costruttore, seguendo il principio di Inversion of Control.
 * Questo facilita il testing e rende il comando facilmente mockabile.</p>
 */
public class ViewMessagesByHashtagCommand implements AdminCommand {

    private static final Logger logger = LoggerFactory.getLogger(ViewMessagesByHashtagCommand.class);

    private final String hashtagName;
    private final MessageRepository messageRepository;
    private List<Message> result;

    /**
     * Costruttore del comando.
     *
     * @param hashtagName il nome dell'hashtag da cercare
     * @param messageRepository il repository per accedere ai dati dei messaggi
     */
    public ViewMessagesByHashtagCommand(String hashtagName, MessageRepository messageRepository) {
        this.hashtagName = hashtagName;
        this.messageRepository = messageRepository;
    }

    /**
     * Esegue la ricerca dei messaggi per hashtag.
     *
     * <p>Passaggi:</p>
     * <ol>
     *   <li>Ricerca i messaggi con l'hashtag specificato.</li>
     *   <li>Memorizza i risultati in un campo privato per il recupero tramite getResult().</li>
     *   <li>Registra l'operazione su log.</li>
     * </ol>
     */
    @Override
    public void execute() {
        logger.info("Inizio esecuzione comando: ViewMessagesByHashtagCommand per hashtag '{}'", hashtagName);

        // 1. Ricerca i messaggi per hashtag
        result = messageRepository.findByHashtag_Name(hashtagName);

        // 2. Log dell'operazione
        logger.info("Ricerca completata: trovati {} messaggi con l'hashtag '{}'",
                result.size(), hashtagName);
    }

    /**
     * Restituisce i messaggi trovati durante l'esecuzione del comando.
     *
     * <p>Questo metodo deve essere chiamato DOPO execute().</p>
     *
     * @return una lista di messaggi con l'hashtag specificato (lista vuota se nessuno trovato)
     */
    public List<Message> getResult() {
        return result != null ? result : List.of();
    }
}
