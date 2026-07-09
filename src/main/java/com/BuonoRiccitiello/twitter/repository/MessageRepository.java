package com.BuonoRiccitiello.twitter.repository;

import com.BuonoRiccitiello.twitter.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Spring Data JPA per la gestione delle entità Message.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Fornisce metodi CRUD (Create, Read, Update, Delete) per i messaggi
 * e query derivate personalizzate.</p>
 *
 * <p><strong>Query personalizzate:</strong></p>
 * <ul>
 *   <li>findByHashtag_Name: Trova i messaggi associati a un hashtag specifico</li>
 * </ul>
 *
 * <p><strong>Utilizzo nella traccia:</strong></p>
 * <p>Il repository è usato dalla TwitterService per salvare messaggi,
 * e dal CommandInvoker per cercare messaggi per hashtag.</p>
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Trova tutti i messaggi associati a un hashtag specifico.
     *
     * <p>Spring Data JPA interpreta il nome del metodo e genera la query SQL:
     * <code>SELECT m FROM Message m WHERE m.hashtag.name = ?1</code></p>
     *
     * @param hashtagName il nome dell'hashtag da cercare
     * @return una lista di messaggi con l'hashtag specificato (lista vuota se nessuno trovato)
     */

    List<Message> findByHashtag_Name(String hashtagName);

    List<Message> findByAuthor_IdInOrderByCreatedAtDesc(List<Long> authorIds);

    /**
     * Trova i messaggi pubblicati da uno specifico autore, dal più recente al meno recente.
     *
     * @param authorId identificativo dell'autore
     * @return messaggi dell'autore ordinati per data decrescente
     */
    List<Message> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);

    /**
     * Conta i messaggi pubblicati da uno specifico autore.
     *
     * @param authorId identificativo dell'autore
     * @return numero di messaggi pubblicati dall'autore
     */
    long countByAuthor_Id(Long authorId);
}

