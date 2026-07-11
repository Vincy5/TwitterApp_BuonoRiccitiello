package com.BuonoRiccitiello.twitter.repository;

import com.BuonoRiccitiello.twitter.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);

    void deleteByRecipient_Id(Long recipientId);

    long countByRecipient_IdAndReadFalse(Long recipientId);

    @Modifying
    @Transactional
    @Query("update Notification n set n.read = true where n.recipient.id = :recipientId and n.read = false")
    int markAllReadByRecipientId(@Param("recipientId") Long recipientId);
}