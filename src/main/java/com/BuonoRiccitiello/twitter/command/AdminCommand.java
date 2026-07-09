package com.BuonoRiccitiello.twitter.command;

/**
 * Interfaccia che definisce il contratto per i comandi amministrativi.
 *
 * <p><strong>Ruolo nel pattern Command:</strong></p>
 * <p>
 * Questa interfaccia rappresenta il ruolo di <strong>Command</strong> nel design pattern Command.
 * Un comando incapsula una richiesta come oggetto, permettendo di parametrizzare i client
 * con diverse operazioni, mettere in coda le richieste, e supportare operazioni undoable.
 * </p>
 *
 * <p><strong>Implementazioni disponibili:</strong></p>
 * <ul>
 *   <li>{@link DeleteUserCommand} - Elimina un utente dal sistema</li>
 *   <li>{@link ViewMessagesByHashtagCommand} - Recupera i messaggi per un hashtag</li>
 *   <li>(Future estensioni) BanUserCommand, ModerateMessageCommand, ecc.</li>
 * </ul>
 *
 * <p><strong>Vantaggi del pattern Command:</strong></p>
 * <ul>
 *   <li><strong>Incapsulamento:</strong> La richiesta è incapsulata come oggetto.</li>
 *   <li><strong>Decoupling:</strong> Il client (AdminController) non conosce i dettagli
 *       dell'implementazione, delegando l'esecuzione al CommandInvoker.</li>
 *   <li><strong>Extensibility:</strong> Aggiungere nuovi comandi è facile senza modificare
 *       il codice del controller.</li>
 *   <li><strong>Logging e Auditing:</strong> È facile loggare e tracciare tutte le operazioni.</li>
 *   <li><strong>Undo/Redo:</strong> Aggiungere storia delle operazioni diventa semplice.</li>
 * </ul>
 *
 * <p><strong>Responsabilità dell'implementazione:</strong></p>
 * <p>Ogni implementazione di AdminCommand deve:</p>
 * <ul>
 *   <li>Implementare la logica specifica del comando nel metodo execute().</li>
 *   <li>Lanciare eccezioni appropriate se qualcosa va storto.</li>
 *   <li>Registrare le operazioni su log per audit trail.</li>
 * </ul>
 */
public interface AdminCommand {

    /**
     * Esegue il comando amministrativo.
     *
     * <p>Questo metodo viene chiamato dal CommandInvoker dopo aver istanziato il comando.
     * La logica specifica dipende dall'implementazione concreta (DeleteUserCommand, ecc.).</p>
     *
     * @throws Exception se il comando non può essere eseguito (es. utente non trovato)
     */
    void execute() throws Exception;
}
