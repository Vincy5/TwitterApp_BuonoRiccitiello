package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.dto.ChangePasswordForm;
import com.BuonoRiccitiello.twitter.dto.MessageForm;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.service.TwitterService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller Spring MVC principale per la gestione dell'interfaccia utente standard.
 * <p>
 * Questa classe coordina i flussi di navigazione della piattaforma simili a un social network,
 * tra cui la timeline della home page, la pubblicazione e l'eliminazione dei messaggi,
 * le dinamiche di follow/unfollow, la visualizzazione del profilo e la gestione dei dati personali
 * dell'account (cambio password e caricamento dell'avatar).
 * </p>
 *
 * @author BuonoRiccitiello
 * @version 1.0
 */
@Controller
@RequestMapping("/")
public class UserController {

    private final TwitterService twitterService;

    /**
     * Costruttore per l'iniezione delle dipendenze di Spring.
     *
     * @param twitterService il servizio delle funzionalità core da iniettare
     */
    public UserController(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    /**
     * Recupera l'utente correntemente loggato dalla sessione HTTP, aggiornando i suoi dati dal database.
     * <p>
     * Questo passaggio intermedio garantisce che qualsiasi mutamento di stato dell'utente (es. contatori,
     * cambio avatar) effettuato in richieste parallele sia immediatamente visibile sul thread corrente.
     * </p>
     *
     * @param session la sessione HTTP da cui estrarre l'attributo dell'utente loggato
     * @return l'entità {@link User} aggiornata, oppure {@code null} se nessun utente è registrato in sessione
     */
    private User getLoggedUserOrNull(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return null;
        }

        User freshUser = twitterService.getUserById(loggedInUser.getId());
        session.setAttribute("loggedInUser", freshUser);
        return freshUser;
    }

    /**
     * Valida ed esegue la sanificazione dell'URL di destinazione per evitare attacchi di Open Redirect.
     * <p>
     * Verifica che la stringa fornita sia un percorso relativo sicuro interno all'applicazione
     * (deve iniziare con un singolo carattere {@code /}).
     * </p>
     *
     * @param returnTo l'URL di destinazione desiderato richiesto dal client
     * @return l'URL sanificato se valido, altrimenti il percorso predefinito {@code /home}
     */
    private String safeRedirect(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/home";
        }
        return returnTo;
    }

    /**
     * Popola il modello Spring MVC con tutti gli attributi condivisi necessari per renderizzare la home page.
     *
     * @param model il contenitore dei dati per la vista
     * @param user  l'utente attualmente autenticato che sta visualizzando il feed
     */
    private void populateHomeModel(Model model, User user) {
        model.addAttribute("loggedInUser", user);
        model.addAttribute("allUsers", twitterService.getAllUsers());
        model.addAttribute("followingIds", twitterService.getFollowingIds(user.getId()));
        model.addAttribute("feedMessages", twitterService.getFeedMessages(user.getId()));
        model.addAttribute("followingCount", twitterService.getFollowingCount(user.getId()));
        model.addAttribute("followersCount", twitterService.getFollowersCount(user.getId()));
        model.addAttribute("publishedMessagesCount", twitterService.getPublishedMessagesCount(user.getId()));
    }

    /**
     * Popola il modello Spring MVC con tutti gli attributi specifici per la gestione della pagina profilo.
     * <p>
     * Determina quale scheda (tab) mostrare tra follower o utenti seguiti, estraendo i relativi elenchi.
     * </p>
     *
     * @param model il contenitore dei dati per la vista
     * @param user  l'utente titolare del profilo
     * @param tab   la scheda attiva selezionata dall'utente (può essere "followers" o "following")
     */
    private void populateProfileModel(Model model, User user, String tab) {
        String activeTab = "followers".equalsIgnoreCase(tab) ? "followers" : "following";
        List<User> visibleUsers = "followers".equals(activeTab)
                ? twitterService.getFollowersUsers(user.getId())
                : twitterService.getFollowingUsers(user.getId());

        model.addAttribute("loggedInUser", user);
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        model.addAttribute("followingIds", twitterService.getFollowingIds(user.getId()));
        model.addAttribute("followingCount", twitterService.getFollowingCount(user.getId()));
        model.addAttribute("followersCount", twitterService.getFollowersCount(user.getId()));
        model.addAttribute("publishedMessagesCount", twitterService.getPublishedMessagesCount(user.getId()));
        model.addAttribute("ownMessages", twitterService.getOwnMessages(user.getId()));
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("profileUsers", visibleUsers);
    }

    /**
     * Mostra la home page dell'applicazione con la timeline dei messaggi.
     *
     * @param session la sessione HTTP per verificare l'autenticazione
     * @param model   il modello per iniettare i dati del feed e della form di pubblicazione
     * @return il template "home" se loggato, altrimenti reindirizza alla pagina di login
     */
    @GetMapping("/home")
    public String showHome(HttpSession session, Model model) {
        User freshUser = getLoggedUserOrNull(session);
        if (freshUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("messageForm", new MessageForm());
        populateHomeModel(model, freshUser);
        return "home";
    }

    /**
     * Visualizza la pagina del profilo personale dell'utente, con schede dedicate alle relazioni.
     *
     * @param tab     parametro opzionale per impostare il focus su "following" o "followers" (default "following")
     * @param session la sessione HTTP per verificare l'autenticazione
     * @param model   il modello per iniettare le informazioni del profilo
     * @return il template "profile" se loggato, altrimenti reindirizza alla pagina di login
     */
    @GetMapping("/profile")
    public String showProfile(
            @RequestParam(value = "tab", required = false, defaultValue = "following") String tab,
            HttpSession session,
            Model model
    ) {
        User freshUser = getLoggedUserOrNull(session);
        if (freshUser == null) {
            return "redirect:/login";
        }

        populateProfileModel(model, freshUser, tab);
        return "profile";
    }

    /**
     * Gestisce l'invio e la pubblicazione di un nuovo messaggio.
     * <p>
     * Se i vincoli di validazione del form falliscono, la pagina "home" viene ricaricata
     * mostrando gli errori inline senza effettuare un redirect.
     * </p>
     *
     * @param messageForm        il DTO contenente i dati del messaggio validati tramite JSR-380
     * @param bindingResult      l'esito della validazione formale dei dati
     * @param session            la sessione HTTP corrente
     * @param model              il modello per re-iniettare i dati in caso di errore di validazione
     * @param redirectAttributes attributi flash per propagare messaggi di successo oltre il redirect
     * @return un redirect alla home se l'invio ha successo, altrimenti ritorna la vista "home" con l'errore
     */
    @PostMapping("/messages")
    public String postMessage(
            @Valid MessageForm messageForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User loggedInUser = getLoggedUserOrNull(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            populateHomeModel(model, loggedInUser);
            return "home";
        }

        try {
            twitterService.postMessage(
                    loggedInUser.getId(),
                    messageForm.getContent(),
                    messageForm.getHashtag(),
                    messageForm.getChannelEnum()
            );

            redirectAttributes.addFlashAttribute("success", "Messaggio pubblicato correttamente.");
            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("error", "Errore nella pubblicazione: " + e.getMessage());
            model.addAttribute("messageForm", new MessageForm());
            populateHomeModel(model, loggedInUser);
            return "home";
        }
    }

    /**
     * Avvia il tracciamento (follow) nei confronti di un altro utente della piattaforma.
     *
     * @param userId             l'identificativo dell'utente da seguire
     * @param returnTo           il percorso relativo a cui ritornare dopo l'azione (es. per restare sulla stessa pagina)
     * @param session            la sessione HTTP corrente
     * @param redirectAttributes attributi flash per i messaggi di esito dell'operazione
     * @return un redirect sicuro all'URL indicato in {@code returnTo}
     */
    @PostMapping("/follow/{id}")
    public String follow(
            @PathVariable("id") Long userId,
            @RequestParam(value = "returnTo", required = false, defaultValue = "/home") String returnTo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loggedInUser = getLoggedUserOrNull(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            twitterService.follow(loggedInUser.getId(), userId);
            redirectAttributes.addFlashAttribute("success", "Utente seguito correttamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Errore follow: " + e.getMessage());
        }

        return "redirect:" + safeRedirect(returnTo);
    }

    /**
     * Interrompe il tracciamento (unfollow) nei confronti di un utente precedentemente seguito.
     *
     * @param userId             l'identificativo dell'utente da smettere di seguire
     * @param returnTo           il percorso relativo a cui ritornare dopo l'azione
     * @param session            la sessione HTTP corrente
     * @param redirectAttributes attributi flash per i messaggi di esito dell'operazione
     * @return un redirect sicuro all'URL indicato in {@code returnTo}
     */
    @PostMapping("/unfollow/{id}")
    public String unfollow(
            @PathVariable("id") Long userId,
            @RequestParam(value = "returnTo", required = false, defaultValue = "/home") String returnTo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loggedInUser = getLoggedUserOrNull(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            twitterService.unfollow(loggedInUser.getId(), userId);
            redirectAttributes.addFlashAttribute("success", "Hai smesso di seguire l'utente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Errore unfollow: " + e.getMessage());
        }

        return "redirect:" + safeRedirect(returnTo);
    }

    /**
     * Consente la rimozione permanente di un messaggio di proprietà dell'utente loggato.
     *
     * @param messageId          l'ID del messaggio da eliminare
     * @param returnTo           il percorso relativo a cui ritornare dopo l'azione
     * @param session            la sessione HTTP corrente
     * @param redirectAttributes attributi flash per i messaggi di esito dell'operazione
     * @return un redirect sicuro all'URL indicato in {@code returnTo}
     */
    @PostMapping("/messages/delete/{id}")
    public String deleteMessage(
            @PathVariable("id") Long messageId,
            @RequestParam(value = "returnTo", required = false, defaultValue = "/home") String returnTo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loggedInUser = getLoggedUserOrNull(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            twitterService.deleteOwnMessage(loggedInUser.getId(), messageId);
            redirectAttributes.addFlashAttribute("success", "Messaggio eliminato correttamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Errore eliminazione messaggio: " + e.getMessage());
        }

        return "redirect:" + safeRedirect(returnTo);
    }

    /**
     * Processa la richiesta di modifica della password dal pannello profilo.
     *
     * @param changePasswordForm DTO contenente password attuale, nuova password e conferma della nuova password
     * @param bindingResult      l'esito della validazione dei campi del modulo
     * @param session            la sessione HTTP corrente
     * @param redirectAttributes attributi flash per comunicare il successo o l'errore dopo il redirect
     * @return un reindirizzamento alla pagina del profilo
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @Valid ChangePasswordForm changePasswordForm,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loggedInUser = getLoggedUserOrNull(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Compila correttamente tutti i campi del cambio password.");
            return "redirect:/profile";
        }

        try {
            twitterService.changePassword(
                    loggedInUser.getId(),
                    changePasswordForm.getCurrentPassword(),
                    changePasswordForm.getNewPassword(),
                    changePasswordForm.getConfirmPassword()
            );
            redirectAttributes.addFlashAttribute("success", "Password aggiornata correttamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Coordina il caricamento e l'assegnazione di un nuovo file d'immagine come avatar del profilo utente.
     *
     * @param avatar             l'oggetto wrapper del file binario inviato dal client multipart form
     * @param session            la sessione HTTP corrente per aggiornare l'utente memorizzato in cache
     * @param redirectAttributes attributi flash per notificare l'avvenuto caricamento
     * @return un reindirizzamento alla pagina del profilo
     */
    @PostMapping("/profile/avatar")
    public String updateAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loggedInUser = getLoggedUserOrNull(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            twitterService.updateProfileImage(loggedInUser.getId(), avatar);
            session.setAttribute("loggedInUser", twitterService.getUserById(loggedInUser.getId()));
            redirectAttributes.addFlashAttribute("success", "Immagine profilo aggiornata correttamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }
}