package com.BuonoRiccitiello.twitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO per il form di registrazione di un nuovo utente.
 *
 * <p>Questo oggetto è mappato dal form HTML register.html e convalidato
 * con le annotazioni di validazione di Jakarta Validation.</p>
 *
 * <p><strong>Validazioni:</strong></p>
 * <ul>
 *   <li>username: obbligatorio, 3-50 caratteri</li>
 *   <li>email: obbligatoria, formato email valido</li>
 *   <li>password: obbligatoria, minimo 6 caratteri</li>
 * </ul>
 */
@Setter
@Getter
public class RegisterForm {

    @NotBlank(message = "L'username è obbligatorio")
    @Size(min = 3, max = 50, message = "L'username deve essere tra 3 e 50 caratteri")
    private String username;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Inserisci un indirizzo email valido")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve avere almeno 6 caratteri")
    private String password;

    @NotBlank(message = "La conferma della password è obbligatoria")
    private String passwordConfirm;
}
