package com.BuonoRiccitiello.twitter.exception;

/**
 * Eccezione base per gli errori dell'applicazione Twitter.
 *
 * Estende {@link RuntimeException} in modo che le eccezioni possano essere
 * lanciate senza obbligare il caller a gestirle con try/catch.
 */
public class TwitterException extends RuntimeException {

    public TwitterException() {
        super();
    }

    public TwitterException(String message) {
        super(message);
    }

    public TwitterException(String message, Throwable cause) {
        super(message, cause);
    }

    public TwitterException(Throwable cause) {
        super(cause);
    }
}
