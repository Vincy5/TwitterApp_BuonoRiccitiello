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
 * Controller per la gestione dell'interfaccia utente standard.
 *
 * <p>Gestisce home, pubblicazione messaggi, follow/unfollow, profilo,
 * cambio password, immagine profilo e cancellazione dei messaggi personali.</p>
 */
@Controller
@RequestMapping("/")
public class UserController {

    private final TwitterService twitterService;

    public UserController(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    /**
     * Recupera l'utente loggato dalla sessione, ricaricandolo dal database.
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
     * Evita redirect esterni o non validi dopo azioni POST.
     */
    private String safeRedirect(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/home";
        }
        return returnTo;
    }

    /**
     * Inserisce nel model tutti i dati comuni della home.
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
     * Inserisce nel model tutti i dati comuni della pagina profilo.
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
     * Visualizza la home page dell'utente loggato.
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
     * Visualizza la pagina profilo separata dalla home.
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
     * Pubblica un nuovo messaggio.
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
     * Segue un altro utente. Il parametro returnTo permette di tornare alla pagina corrente.
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
     * Smette di seguire un altro utente. Il parametro returnTo permette di tornare alla pagina corrente.
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
     * Elimina un messaggio pubblicato dall'utente loggato.
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
     * Aggiorna la password dalla pagina profilo.
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
     * Aggiorna l'immagine profilo caricata dall'utente.
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
