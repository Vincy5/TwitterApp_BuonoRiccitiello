package com.BuonoRiccitiello.twitter.model;

/**
 * Canali disponibili per l'invio di un messaggio.
 *
 * <p>I canali rappresentano i diversi mezzi attraverso i quali un messaggio può essere trasmesso:
 * <ul>
 *   <li><strong>WEB:</strong> Invio tramite interfaccia web</li>
 *   <li><strong>SMS:</strong> Invio tramite SMS</li>
 *   <li><strong>EMAIL:</strong> Invio tramite email</li>
 *   <li><strong>IM:</strong> Invio tramite instant messaging (chat, Telegram, ecc.)</li>
 * </ul>
 *
 * <p>Ogni canale ha un'implementazione corrispondente nel package factory
 * che gestisce l'invio effettivo del messaggio.</p>
 */
public enum Channel {
    WEB,
    SMS,
    EMAIL,
    IM
}
