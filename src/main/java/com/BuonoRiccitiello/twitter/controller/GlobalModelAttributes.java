package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.service.TwitterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {

    private final TwitterService twitterService;

    public GlobalModelAttributes(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    @ModelAttribute
    public void addAttributes(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            long unread = twitterService.getUnreadNotificationsCount(loggedInUser.getId());
            model.addAttribute("hasUnreadNotifications", unread > 0);
            model.addAttribute("unreadNotificationsCount", unread);
            model.addAttribute("loggedInUser", loggedInUser);
        }
    }
}

