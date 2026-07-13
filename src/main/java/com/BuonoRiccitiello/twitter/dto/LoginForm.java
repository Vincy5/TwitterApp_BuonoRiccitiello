package com.BuonoRiccitiello.twitter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO per il form di login.
 *
 * <p>Questo oggetto è mappato dal form HTML login.html e convalidato
 * con le annotazioni di validazione di Jakarta Validation.</p>
 *
 * <p><strong>Validazioni:</strong></p>
 * <ul>
 *   <li>username: obbligatorio</li>
 *   <li>password: obbligatoria</li>
 * </ul>
 */
@Setter
@Getter
public class LoginForm {

    @NotBlank(message = "L'username è obbligatorio")
    private String username;

    @NotBlank(message = "La password è obbligatoria")
    private String password;

}
