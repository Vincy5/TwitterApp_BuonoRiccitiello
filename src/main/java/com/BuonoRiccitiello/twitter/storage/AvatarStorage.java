package com.BuonoRiccitiello.twitter.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Astrazione per la gestione del salvataggio delle immagini degli avatar utente.
 * <p>
 * Questa interfaccia definisce il contratto per i componenti responsabili della persistenza
 * dei file multimediali legati al profilo utente.
 * </p>
 *
 */
public interface AvatarStorage {

    /**
     * Salva l'avatar di un utente e restituisce il percorso pubblico per accedervi.
     * <p>
     * Le classi che implementano questo metodo si occupano di convalidare il file
     * (verificando ad esempio il tipo MIME, la dimensione massima o l'estensione) e di
     * memorizzarlo associandolo in modo univoco all'identificativo dell'utente.
     * </p>
     *
     * @param userId l'ID dell'utente a cui associare il nuovo avatar
     * @param avatar il file dell'immagine caricato dall'utente tramite form multimediale
     * @return una {@link String} che rappresenta il percorso pubblico o l'URL per accedere al file
     *         (es. {@code /uploads/avatars/user-123.png})
     * @throws IOException se si verifica un errore generico di I/O durante la scrittura del file
     * @throws IllegalArgumentException se il file non è valido (es. formato non supportato,
     *                                  file vuoto o dimensioni sopra il limite consentito)
     */
    String storeAvatar(Long userId, MultipartFile avatar) throws IOException;
}