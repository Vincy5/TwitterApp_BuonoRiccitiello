package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.service.TwitterService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.mockito.Mockito.*;

class GlobalModelAttributesTest {

    @Mock
    private TwitterService twitterService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    private GlobalModelAttributes advice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        advice = new GlobalModelAttributes(twitterService);
    }

    @Test
    void addsUnreadAttributesWhenUserPresent() {
        User u = new User();
        u.setId(5L);
        when(session.getAttribute("loggedInUser")).thenReturn(u);
        when(twitterService.getUnreadNotificationsCount(5L)).thenReturn(3L);

        advice.addAttributes(model, session);

        verify(model).addAttribute("hasUnreadNotifications", true);
        verify(model).addAttribute("unreadNotificationsCount", 3L);
        verify(model).addAttribute("loggedInUser", u);
    }
}

