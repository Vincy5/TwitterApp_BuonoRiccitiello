package com.BuonoRiccitiello.twitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO per il cambio password dalla pagina profilo.
 */
public class ChangePasswordForm {

    @NotBlank(message = "La password attuale è obbligatoria")
    private String currentPassword;

    @NotBlank(message = "La nuova password è obbligatoria")
    @Size(min = 6, message = "La nuova password deve avere almeno 6 caratteri")
    private String newPassword;

    @NotBlank(message = "La conferma della nuova password è obbligatoria")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
