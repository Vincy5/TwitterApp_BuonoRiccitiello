package com.BuonoRiccitiello.twitter.dto;

import jakarta.validation.constraints.NotBlank;

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
public class LoginForm {

    @NotBlank(message = "L'username è obbligatorio")
    private String username;

    @NotBlank(message = "La password è obbligatoria")
    private String password;

    // Getter e Setter

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
