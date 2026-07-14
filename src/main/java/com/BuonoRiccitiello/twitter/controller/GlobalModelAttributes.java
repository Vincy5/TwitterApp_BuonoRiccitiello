package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.service.TwitterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/**
 * Classe controller advice per l'aggiunta di attributi globali al modello.
 * 
 * <p> Fornisce attributi comuni a tutte le viste, tra cui il numero di notifiche non lette
 * e i dati dell'utente attualmente loggato. Viene applicata a livello globale grazie
 * all'annotazione @ControllerAdvice. </p>
 *
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final TwitterService twitterService;

    /**
     * Costruttore che inizializza il servizio Twitter.
     * 
     * @param twitterService il servizio Twitter da iniettare
     */
    public GlobalModelAttributes(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    /**
     * Aggiunge attributi globali al modello per tutte le viste.
     * 
     * Recupera l'utente loggato dalla sessione e, se presente, calcola il numero
     * di notifiche non lette e le aggiunge al modello insieme alle informazioni
     * dell'utente.
     * 
     * @param model il modello dove aggiungere gli attributi
     * @param session la sessione HTTP corrente da cui estrarre l'utente loggato
     */
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

