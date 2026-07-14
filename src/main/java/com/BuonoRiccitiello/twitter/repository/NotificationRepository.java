package com.BuonoRiccitiello.twitter.repository;

import com.BuonoRiccitiello.twitter.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository JPA per la gestione ed persistenza delle entità {@link Notification}.
 * <p>
 * Questa interfaccia estende {@link JpaRepository} per fornire le operazioni CRUD standard
 * e definisce query personalizzate per la gestione delle notifiche
 * associate a uno specifico utente destinatario.
 * </p>
 *
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Recupera tutte le notifiche di un determinato destinatario, ordinate dalla più recente.
     * <p>
     * Sfrutta la query derivation di Spring Data analizzando le proprietà {@code recipient.id}
     * e {@code createdAt} dell'entità per generare automaticamente l'ordinamento decrescente.
     * </p>
     *
     * @param recipientId l'ID dell'utente destinatario delle notifiche
     * @return una {@link List} di {@link Notification} ordinate per data di creazione decrescente
     */
    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);

    /**
     * Elimina tutte le notifiche associate a un determinato destinatario.
     * <p>
     * Metodo utile in scenari di pulizia dati o quando un account utente viene rimosso
     * dal sistema.
     * </p>
     *
     * @param recipientId l'ID dell'utente di cui eliminare le notifiche
     */
    void deleteByRecipient_Id(Long recipientId);

    /**
     * Conta il numero di notifiche non ancora lette per un determinato destinatario.
     * <p>
     * Viene tipicamente utilizzato per mostrare il badge numerico (o "pallino")
     * delle notifiche nell'interfaccia utente.
     * </p>
     *
     * @param recipientId l'ID dell'utente destinatario
     * @return il numero totale di notifiche con stato {@code read = false}
     */
    long countByRecipient_IdAndReadFalse(Long recipientId);

    /**
     * Aggiorna lo stato di tutte le notifiche non lette di un utente, impostandole come lette.
     * <p>
     * L'operazione viene eseguita tramite una query JPQL personalizzata di tipo UPDATE.
     * Richiede l'annotazione {@link Modifying} poiché modifica lo stato del database e
     * {@link Transactional} per garantire l'atomicità dell'operazione di scrittura.
     * </p>
     *
     * @param recipientId l'ID dell'utente destinatario per cui marcare le notifiche
     * @return il numero di record effettivamente aggiornati nel database
     */
    @Modifying
    @Transactional
    @Query("update Notification n set n.read = true where n.recipient.id = :recipientId and n.read = false")
    int markAllReadByRecipientId(@Param("recipientId") Long recipientId);
}