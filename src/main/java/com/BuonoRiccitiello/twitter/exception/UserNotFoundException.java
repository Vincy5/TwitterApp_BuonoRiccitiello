package com.BuonoRiccitiello.twitter.exception;

/**
 * Eccezione sollevata quando un utente richiesto non viene trovato nel sistema.
 *
 * Esempi di utilizzo: ricerca per id o username che non restituiscono risultati.
 */
public class UserNotFoundException extends TwitterException {

    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}
