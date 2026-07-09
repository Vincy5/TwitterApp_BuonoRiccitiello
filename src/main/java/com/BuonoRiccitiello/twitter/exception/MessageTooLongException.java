package com.BuonoRiccitiello.twitter.exception;

/**
 * Eccezione sollevata quando il contenuto di un messaggio supera la lunghezza
 * massima consentita (ad esempio oltre 140 caratteri).
 */
public class MessageTooLongException extends TwitterException {

    public MessageTooLongException() {
        super();
    }

    public MessageTooLongException(String message) {
        super(message);
    }

    public MessageTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageTooLongException(Throwable cause) {
        super(cause);
    }
}
