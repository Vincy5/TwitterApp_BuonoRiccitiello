package com.BuonoRiccitiello.twitter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller per la gestione dell'index (root path).
 *
 * <p>Reindirizza l'accesso a / verso /login.</p>
 */
@Controller
public class IndexController {

    /**
     * Reindirizza il root path a /login.
     *
     * @return redirect a /login
     */
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}
