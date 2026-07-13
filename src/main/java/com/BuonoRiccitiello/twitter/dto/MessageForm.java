package com.BuonoRiccitiello.twitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.BuonoRiccitiello.twitter.model.Channel;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO per il form di pubblicazione di un messaggio.
 *
 * <p>Questo oggetto è mappato dal form HTML home.html e convalidato
 * con le annotazioni di validazione di Jakarta Validation.</p>
 *
 * <p><strong>Validazioni:</strong></p>
 * <ul>
 *   <li>content: obbligatorio, max 140 caratteri</li>
 *   <li>hashtag: opzionale</li>
 *   <li>channel: obbligatorio (WEB, SMS, EMAIL, IM)</li>
 * </ul>
 */
@Setter
@Getter
public class MessageForm {

    @NotBlank(message = "Il contenuto del messaggio è obbligatorio")
    @Size(max = 140, message = "Il messaggio non può superare 140 caratteri")
    private String content;

    private String hashtag; // Opzionale

    @NotBlank(message = "Seleziona un canale di invio")
    private String channel; // Enum come stringa: WEB, SMS, EMAIL, IM

    /**
     * Converte la stringa del canale in enum Channel.
     *
     * @return l'enum Channel corrispondente
     * @throws IllegalArgumentException se il canale non è valido
     */
    public Channel getChannelEnum() {
        return Channel.valueOf(channel.toUpperCase());
    }
}
