package com.BuonoRiccitiello.twitter.controller;

import com.BuonoRiccitiello.twitter.builder.UserBuilder;
import com.BuonoRiccitiello.twitter.dto.LoginForm;
import com.BuonoRiccitiello.twitter.dto.RegisterForm;
import com.BuonoRiccitiello.twitter.exception.UserAlreadyExistsException;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.service.AuthService;
import com.BuonoRiccitiello.twitter.service.TwitterService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

/**
 * Controller per la gestione dell'autenticazione degli utenti.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Gestire la visualizzazione e l'elaborazione del form di login.</li>
 *   <li>Gestire la visualizzazione e l'elaborazione del form di registrazione.</li>
 *   <li>Gestire il logout dell'utente.</li>
 *   <li>Gestire la sessione HTTP per memorizzare l'utente loggato.</li>
 * </ul>
 *
 * <p><strong>Session Management:</strong></p>
 * <p>Usiamo HttpSession semplice per l'autenticazione. In un'applicazione reale,
 * si userebbe Spring Security con support completo per session, CSRF, ecc.</p>
 */
@Controller
@RequestMapping
public class AuthController {

    private final TwitterService twitterService;
    private final AuthService authService;

    /**
     * Costruttore con dependency injection.
     */
    public AuthController(TwitterService twitterService, AuthService authService) {
        this.twitterService = twitterService;
        this.authService = authService;
    }

    /**
     * Visualizza il form di login.
     *
     * @param model il modello per passare dati al template
     * @return il nome del template Thymeleaf "login"
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    /**
     * Elabora il submit del form di login.
     *
     * <p>Se l'autenticazione ha successo, salva l'utente nella sessione e
     * reindirizza a /home. Altrimenti, torna al form di login con un messaggio di errore.</p>
     *
     * @param loginForm il form con username e password
     * @param bindingResult il risultato della validazione
     * @param session la sessione HTTP
     * @param model il modello per passare dati al template
     * @return redirect a /home se il login ha successo, altrimenti torna a "login"
     */
    @PostMapping("/login")
    public String login(
            @Valid LoginForm loginForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {
        // Validazione del form
        if (bindingResult.hasErrors()) {
            return "login";
        }

        // Tenta l'autenticazione
        Optional<User> user = authService.login(loginForm.getUsername(), loginForm.getPassword());

        if (user.isPresent()) {
            // Login riuscito: salva l'utente nella sessione
            session.setAttribute("loggedInUser", user.get());
            return "redirect:/home";
        } else {
            // Login fallito: mostra messaggio di errore
            model.addAttribute("error", "Username o password non validi");
            return "login";
        }
    }

    /**
     * Visualizza il form di registrazione.
     *
     * @param model il modello per passare dati al template
     * @return il nome del template Thymeleaf "register"
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    /**
     * Elabora il submit del form di registrazione.
     *
     * <p>Se la registrazione ha successo, l'utente viene salvato nel database
     * e reindirizzato a /login. Se fallisce (username già esistente, dati non validi, ecc.),
     * torna al form di registrazione con un messaggio di errore.</p>
     *
     * @param registerForm il form con i dati di registrazione
     * @param bindingResult il risultato della validazione
     * @param model il modello per passare dati al template
     * @return redirect a /login se la registrazione ha successo, altrimenti torna a "register"
     */
    @PostMapping("/register")
    public String register(
            @Valid RegisterForm registerForm,
            BindingResult bindingResult,
            Model model
    ) {
        // Validazione del form
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Verifica che le password corrispondano
        if (!registerForm.getPassword().equals(registerForm.getPasswordConfirm())) {
            model.addAttribute("error", "Le password non corrispondono");
            return "register";
        }

        try {
            // Delega al service per la registrazione (che codificherà la password e salverà a DB)
            // Nota: il service accede direttamente ai dati via getRawPassword()
            twitterService.registerUser(new UserBuilder()
                    .withUsername(registerForm.getUsername())
                    .withEmail(registerForm.getEmail())
                    .withPassword(registerForm.getPassword())
            );

            // Registrazione riuscita: reindirizza a login
            model.addAttribute("success", "Registrazione avvenuta con successo! Effettua il login.");
            return "redirect:/login";
        } catch (UserAlreadyExistsException e) {
            // Username già registrato
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            // Errore generico
            model.addAttribute("error", "Errore durante la registrazione: " + e.getMessage());
            return "register";
        }
    }

    /**
     * Effettua il logout dell'utente.
     *
     * @param session la sessione HTTP
     * @return redirect a /login
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
