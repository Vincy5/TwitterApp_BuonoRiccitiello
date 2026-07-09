package com.BuonoRiccitiello.twitter.exception;

/**
 * Eccezione sollevata quando un'azione richiesta dall'utente non è autorizzata,
 * ad esempio modifica/eliminazione di un messaggio di un altro utente.
 */
public class UnauthorizedActionException extends TwitterException {

    public UnauthorizedActionException() {
        super();
    }

    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedActionException(Throwable cause) {
        super(cause);
    }
}
