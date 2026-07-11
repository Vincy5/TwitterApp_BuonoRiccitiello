package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.model.Notification;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificationControllerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new NotificationController(notificationRepository);
    }

    @Test
    void viewNotifications_marksReadAndAddsAttributes() {
        User user = new User();
        user.setId(7L);
        when(session.getAttribute("loggedInUser")).thenReturn(user);

        when(notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(7L)).thenReturn(List.of());

        String view = controller.viewNotifications(session, model);

        assertEquals("notifications", view);
        verify(notificationRepository, times(1)).markAllReadByRecipientId(7L);
        verify(notificationRepository, times(1)).findByRecipient_IdOrderByCreatedAtDesc(7L);
        verify(model).addAttribute("loggedInUser", user);
        verify(model).addAttribute("notifications", List.of());
        verify(model).addAttribute("hasUnreadNotifications", false);
        verify(model).addAttribute("unreadNotificationsCount", 0);
    }
}

