package com.BuonoRiccitiello.twitter.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Implementazione concreta di {@link AvatarStorage} per il salvataggio su File System locale.
 * <p>
 * Questa classe gestisce la memorizzazione fisica delle immagini degli avatar all'interno
 * di una cartella locale del server, occupandosi di validare il file (tipo, dimensione, estensione)
 * e di sovrascrivere l'eventuale avatar precedente dello stesso utente.
 * </p>
 *
 */
@Service
public class FileSystemAvatarStorage implements AvatarStorage {

    /** Limite massimo di dimensione del file consentito (2 Megabyte). */
    private static final long MAX_BYTES = 2L * 1024L * 1024L; // 2MB

    /**
     * Salva l'avatar dell'utente sul file system locale, effettuando controlli di sicurezza.
     * <p>
     * Il file viene rinominato in modo standard (es. {@code user-123.jpg}) per evitare
     * conflitti di nomi e problemi di sicurezza legati ai caratteri speciali nei file originali.
     * </p>
     *
     * @param userId l'ID dell'utente a cui associare il nuovo avatar
     * @param avatar il file dell'immagine caricato dall'utente
     * @return il percorso URL relativo (es. {@code /uploads/avatars/user-123.png}) utilizzabile dal client web
     * @throws IOException se si verificano errori di scrittura sul disco o di creazione delle cartelle
     * @throws IllegalArgumentException se il file è assente, non è un'immagine, o supera i 2 MB
     */
    @Override
    public String storeAvatar(Long userId, MultipartFile avatar) throws IOException {
        // 1. Controllo di presenza: verifica che il file non sia nullo o vuoto
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Seleziona un'immagine da caricare.");
        }

        // 2. Controllo del Content Type: verifica che il MIME type inizi con "image/" (es. image/jpeg, image/png)
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Il file caricato deve essere un'immagine.");
        }

        // 3. Controllo della dimensione: blocca file superiori al limite stabilito (2MB)
        if (avatar.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Dimensione immagine eccessiva. Max 2 MB.");
        }

        // 4. Estrazione sicura dell'estensione del file originale
        String originalFilename = avatar.getOriginalFilename();
        String extension = ".png"; // Estensione di fallback predefinita
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }

        // 5. Sanificazione dell'estensione: se non rientra nei formati grafici web standard, forza .png
        if (!extension.matches("\\.(png|jpg|jpeg|gif|webp)")) {
            extension = ".png";
        }

        // 6. Definizione del percorso di destinazione (cartella 'uploads/avatars' nella radice del progetto)
        Path uploadDir = Paths.get("uploads", "avatars");
        // Crea le directory se non esistono ancora sul disco
        Files.createDirectories(uploadDir);

        // 7. Generazione del nome file univoco basato sull'ID utente (garantisce una sola immagine per utente)
        String filename = "user-" + userId + extension;
        Path destination = uploadDir.resolve(filename);

        // 8. Copia fisica del flusso di dati dell'immagine, sovrascrivendo l'eventuale file preesistente
        Files.copy(avatar.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Ritorna il path virtuale mappato nel WebConfig per l'accesso HTTP dal browser
        return "/uploads/avatars/" + filename;
    }
}