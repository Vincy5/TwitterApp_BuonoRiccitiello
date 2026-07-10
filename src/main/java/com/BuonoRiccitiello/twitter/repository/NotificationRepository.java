package com.BuonoRiccitiello.twitter.repository;

import com.BuonoRiccitiello.twitter.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);

    void deleteByRecipient_Id(Long recipientId);
}