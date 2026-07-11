package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.model.Notification;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/notifications")
    public String viewNotifications(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Prima, marca tutte le notifiche come lette per l'utente corrente
        try {
            notificationRepository.markAllReadByRecipientId(loggedInUser.getId());
        } catch (Exception ignore) {
            // In caso di errore non blocchiamo la visualizzazione
        }

        List<Notification> notifications =
                notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(loggedInUser.getId());

        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("notifications", notifications);
        // Sovrascrivo l'attributo globale per rimuovere il pallino nelle pagine dopo aver letto
        model.addAttribute("hasUnreadNotifications", false);
        model.addAttribute("unreadNotificationsCount", 0);

        return "notifications";
    }
}