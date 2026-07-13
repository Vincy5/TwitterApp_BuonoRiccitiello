package com.BuonoRiccitiello.twitter.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Gestore globale delle eccezioni che intercetta le eccezioni dell'applicazione
 * e reindirizza a una pagina di errore leggibile dall'utente invece di mostrare
 * lo stack trace.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("errorMessage", "Utente non trovato: " + ex.getMessage());
        return mv;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserExists(UserAlreadyExistsException ex) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("errorMessage", "Impossibile creare l'utente: utente già esistente.");
        return mv;
    }

    @ExceptionHandler(MessageTooLongException.class)
    public ModelAndView handleMessageTooLong(MessageTooLongException ex) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("errorMessage", "Il messaggio è troppo lungo. Massimo 140 caratteri.");
        return mv;
    }

    @ExceptionHandler(TwitterException.class)
    public ModelAndView handleGeneric(TwitterException ex) {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("errorMessage", "Si è verificato un errore. Riprova più tardi.");
        return mv;
    }
}
