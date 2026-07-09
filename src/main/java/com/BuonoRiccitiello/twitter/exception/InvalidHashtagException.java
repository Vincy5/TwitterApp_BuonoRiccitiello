package com.BuonoRiccitiello.twitter.exception;

/**
 * Eccezione sollevata quando un hashtag non è valido o non è utilizzabile
 * per qualche motivo (ad esempio formato scorretto o non esistente).
 */
public class InvalidHashtagException extends TwitterException {

    public InvalidHashtagException() {
        super();
    }

    public InvalidHashtagException(String message) {
        super(message);
    }

    public InvalidHashtagException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidHashtagException(Throwable cause) {
        super(cause);
    }
}
