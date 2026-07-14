package com.BuonoRiccitiello.twitter.repository;

import com.BuonoRiccitiello.twitter.model.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA per la gestione delle entità Hashtag.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Fornisce metodi CRUD (Create, Read, Update, Delete) per gli hashtag
 * e query derivate personalizzate.</p>
 */
@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    /**
     * Trova un hashtag per nome.
     *
     * @param name il nome dell'hashtag da cercare
     * @return Optional contenente l'hashtag se trovato, altrimenti Optional.empty()
     */
    Optional<Hashtag> findByName(String name);
}
