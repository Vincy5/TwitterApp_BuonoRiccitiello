package com.BuonoRiccitiello.twitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO per il cambio password dalla pagina profilo.
 */
@Setter
@Getter
public class ChangePasswordForm {

    @NotBlank(message = "La password attuale è obbligatoria")
    private String currentPassword;

    @NotBlank(message = "La nuova password è obbligatoria")
    @Size(min = 6, message = "La nuova password deve avere almeno 6 caratteri")
    private String newPassword;

    @NotBlank(message = "La conferma della nuova password è obbligatoria")
    private String confirmPassword;

}
