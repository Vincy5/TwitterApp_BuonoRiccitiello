package com.BuonoRiccitiello.twitter.repository;

import com.BuonoRiccitiello.twitter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository Spring Data JPA per la gestione delle entità User.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <p>Fornisce metodi CRUD (Create, Read, Update, Delete) per gli utenti
 * e query derivate personalizzate.</p>
 *
 * <p><strong>Perché Spring Data JPA soddisfa il requisito di database:</strong></p>
 * <ul>
 *   <li>Implementa il pattern Repository, astrayendo i dettagli di accesso ai dati.</li>
 *   <li>Spring Data JPA genera automaticamente le query SQL partendo dai nomi dei metodi.</li>
 *   <li>H2 su file (non in-memory) garantisce la persistenza tra i riavvii dell'applicazione.</li>
 *   <li>Nessuna necessità di script SQL manuale: JPA crea le tabelle automaticamente
 *       tramite hibernate.ddl-auto=update in application.properties.</li>
 * </ul>
 *
 * <p><strong>Utilizzo nella traccia:</strong></p>
 * <p>La traccia richiede "usare i file o database". H2 su file soddisfa pienamente
 * questo requisito, mantenendo i dati in un file (./data/twitterdb.mv.db) invece
 * di mantenerli in memoria volatile.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trova un utente per username.
     *
     * @param username lo username da cercare
     * @return Optional contenente l'utente se trovato, altrimenti Optional.empty()
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifica se uno username è già registrato.
     *
     * @param username lo username da verificare
     * @return true se lo username esiste, false altrimenti
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se una email è già registrata.
     *
     * @param email l'email da verificare
     * @return true se l'email esiste, false altrimenti
     */
    boolean existsByEmail(String email);

    @Modifying
    @Query(value = "DELETE FROM user_following WHERE user_id = :userId OR following_id = :userId", nativeQuery = true)
    void deleteAllFollowRelationsForUser(@Param("userId") Long userId);
}
