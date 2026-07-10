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

        List<Notification> notifications =
                notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(loggedInUser.getId());

        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("notifications", notifications);

        return "notifications";
    }
}