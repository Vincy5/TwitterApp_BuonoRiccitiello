package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.exception.UserNotFoundException;
import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.service.TwitterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per le operazioni amministrative.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Visualizzare la pagina di amministrazione.</li>
 *   <li>Eliminare utenti dal sistema.</li>
 *   <li>Ricercare messaggi per hashtag.</li>
 * </ul>
 *
 * <p><strong>Autenticazione e Autorizzazione:</strong></p>
 * <p>Tutti i metodi verificano che l'utente sia loggato e sia un ADMIN.
 * Se non loggato o non admin, l'utente viene reindirizzato a /login.</p>
 *
 * <p><strong>Nota sulla sicurezza:</strong></p>
 * <p>In un'applicazione reale, si userebbe Spring Security per controllare
 * l'autorizzazione. Per semplicità, verifichiamo solo il role qui nel controller.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TwitterService twitterService;

    /**
     * Costruttore con dependency injection.
     */
    public AdminController(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    /**
     * Visualizza la pagina di amministrazione.
     *
     * <p>Mostra una tabella di tutti gli utenti con i pulsanti per eliminarli,
     * e un form per cercare messaggi per hashtag.</p>
     *
     * @param session la sessione HTTP
     * @param model il modello per passare dati al template
     * @return il nome del template Thymeleaf "admin", oppure redirect a /login se non loggato
     */
    @GetMapping
    public String showAdminPanel(HttpSession session, Model model) {
        // Verifica che l'utente sia loggato
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Verifica che l'utente sia ADMIN
        if (!"ADMIN".equals(loggedInUser.getRole().toString())) {
            return "redirect:/home"; // Reindirizza a home se non admin
        }

        // Passa i dati al template
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("allUsers", twitterService.getAllUsers());

        return "admin";
    }

    /**
     * Elimina un utente dal sistema (operazione amministrativa).
     *
     * <p>Quando un utente viene eliminato, tutti i suoi follower ricevono
     * una notifica tramite il pattern Observer.</p>
     *
     * @param userId l'ID dell'utente da eliminare
     * @param session la sessione HTTP
     * @param model il modello per passare dati al template
     * @return redirect a /admin
     */
    @DeleteMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable("id") Long userId,
            HttpSession session,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        // Verifica che l'utente sia loggato e admin
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(loggedInUser.getRole().toString())) {
            return "redirect:/home";
        }

        try {
            // Elimina l'utente
            twitterService.adminDeleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "Utente eliminato con successo");
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Utente non trovato: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Errore durante l'eliminazione: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    /**
     * Ricerca messaggi per hashtag (operazione amministrativa).
     *
     * <p>Ritorna una lista di messaggi associati all'hashtag specificato.</p>
     *
     * @param hashtagName il nome dell'hashtag da cercare
     * @param session la sessione HTTP
     * @param model il modello per passare dati al template
     * @return il nome del template Thymeleaf "admin" con i risultati
     */
    @GetMapping("/hashtag/{name}")
    public String viewByHashtag(
            @PathVariable("name") String hashtagName,
            HttpSession session,
            Model model
    ) {
        // Verifica che l'utente sia loggato e admin
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(loggedInUser.getRole().toString())) {
            return "redirect:/home";
        }

        try {
            // Ricerca i messaggi per hashtag
            List<Message> messages = twitterService.adminViewByHashtag(hashtagName);

            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("allUsers", twitterService.getAllUsers());
            model.addAttribute("searchResults", messages);
            model.addAttribute("searchHashtag", hashtagName);

            if (messages.isEmpty()) {
                model.addAttribute("info", "Nessun messaggio trovato per l'hashtag '" + hashtagName + "'");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Errore nella ricerca: " + e.getMessage());
        }

        return "admin";
    }
}
