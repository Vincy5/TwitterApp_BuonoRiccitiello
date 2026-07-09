package com.BuonoRiccitiello.twitter.exception;

/**
 * Eccezione sollevata quando si tenta di creare un utente con username o email
 * già presenti nel sistema.
 */
public class UserAlreadyExistsException extends TwitterException {

    public UserAlreadyExistsException() {
        super();
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
