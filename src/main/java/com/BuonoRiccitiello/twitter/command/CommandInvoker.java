package com.BuonoRiccitiello.twitter.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Invoker che esegue i comandi amministrativi.
 *
 * <p><strong>Ruolo nel pattern Command:</strong></p>
 * <p>
 * Questa classe rappresenta il ruolo di <strong>Invoker</strong> nel design pattern Command.
 * L'invoker riceve comandi dal client (es. AdminController) e li esegue, nascondendo
 * i dettagli implementativi al client stesso.
 * </p>
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Ricevere i comandi dal client.</li>
 *   <li>Eseguire i comandi.</li>
 *   <li>Gestire gli errori.</li>
 *   <li>Registrare le operazioni su log per audit trail.</li>
 * </ul>
 *
 * <p><strong>Principio Dependency Inversion:</strong></p>
 * <p>
 * Il client (AdminController) dipende dall'interfaccia AdminCommand, non dalle
 * implementazioni concrete (DeleteUserCommand, ViewMessagesByHashtagCommand).
 * Questo permette di aggiungere nuovi comandi senza modificare il controller.
 * </p>
 *
 * <p><strong>Vantaggi dell'Invoker pattern:</strong></p>
 * <ul>
 *   <li>Centralizzazione della logica di esecuzione e gestione errori.</li>
 *   <li>Facilità di aggiungere feature trasversali (logging, timing, retry, ecc.).</li>
 *   <li>Separazione tra il client e i comandi concreti.</li>
 *   <li>Facilità di testare i comandi in isolamento dal controller.</li>
 * </ul>
 */
@Component
public class CommandInvoker {

    private static final Logger logger = LoggerFactory.getLogger(CommandInvoker.class);

    /**
     * Esegue un comando amministrativo.
     *
     * <p>Questo metodo cattura gli errori e li registra su log, fornendo
     * un'interfaccia uniforme per l'esecuzione di comandi eterogenei.</p>
     *
     * @param command il comando da eseguire
     * @return true se il comando è stato eseguito con successo, false altrimenti
     */
    public boolean executeCommand(AdminCommand command) {
        try {
            logger.debug("Esecuzione comando: {}", command.getClass().getSimpleName());
            command.execute();
            logger.info("Comando {} eseguito con successo", command.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione del comando {}: {}",
                    command.getClass().getSimpleName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Esegue un comando e restituisce il risultato (per comandi che generano output).
     *
     * <p>Questo metodo è utile per comandi come ViewMessagesByHashtagCommand
     * che producono un risultato da restituire al client.</p>
     *
     * @param command il comando da eseguire
     * @return il risultato del comando, oppure null se si verifica un errore
     */
    public Object executeCommandAndGetResult(AdminCommand command) {
        try {
            logger.debug("Esecuzione comando con risultato: {}", command.getClass().getSimpleName());
            command.execute();
            logger.info("Comando {} eseguito con successo", command.getClass().getSimpleName());

            // Se il comando implementa l'interfaccia HasResult, ritorna il risultato
            if (command instanceof ViewMessagesByHashtagCommand) {
                return ((ViewMessagesByHashtagCommand) command).getResult();
            }

            return null;
        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione del comando {}: {}",
                    command.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
}
