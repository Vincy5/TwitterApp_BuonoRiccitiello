package com.BuonoRiccitiello.twitter.builder;

//import com.BuonoRiccitiello.twitter.exception.UserAlreadyExistsException;
import com.BuonoRiccitiello.twitter.model.Role;
import com.BuonoRiccitiello.twitter.model.User;

import java.util.HashSet;

/**
 * Implementazione del design pattern Builder per la creazione di entità User.
 *
 * <p><strong>Motivazione della scelta del Builder:</strong></p>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP):</strong> Separa la logica di creazione
 *       e validazione di un User dalla sua logica di dominio.</li>
 *   <li><strong>Leggibilità:</strong> I metodi fluenti (withUsername, withEmail, withPasswordHash, withRole)
 *       rendono il codice autoesplicativo senza costringere a usare costruttori con molti parametri.</li>
 *   <li><strong>Validazione centralizzata:</strong> Tutte le regole di validazione (email, lunghezza username, ecc.)
 *       sono concentrate nel metodo build().</li>
 *   <li><strong>Flessibilità:</strong> Permette di creare User con diversi insiemi di attributi,
 *       e assegna automaticamente valori di default (es. Role.UTENTE se non specificato).</li>
 * </ul>
 *
 * <p><strong>Utilizzo:</strong></p>
 * <pre>
 *   User user = new UserBuilder()
 *       .withUsername("andreaR")
 *       .withEmail("andrea@example.com")
 *       .withPasswordHash("$2a$10$...")  // BCrypt hash
 *       .withRole(Role.UTENTE)
 *       .build();
 * </pre>
 */
public class UserBuilder {

    private String username;
    private String email;
    private String passwordHash;
    private String rawPassword; // Memorizza la password in chiaro prima della codifica
    private Role role;

    /**
     * Imposta l'username dell'utente (obbligatorio).
     *
     * @param username il nome utente (deve essere univoco)
     * @return this (per concatenamento fluente)
     */
    public UserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Imposta l'email dell'utente (obbligatorio).
     *
     * @param email l'indirizzo email
     * @return this (per concatenamento fluente)
     */
    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Imposta la password in chiaro dell'utente.
     *
     * <p>La password sarà codificata nel service layer usando BCryptPasswordEncoder
     * prima di essere salvata nel database.</p>
     *
     * @param rawPassword la password in chiaro
     * @return this (per concatenamento fluente)
     */
    public UserBuilder withPassword(String rawPassword) {
        this.rawPassword = rawPassword;
        return this;
    }

    /**
     * Imposta l'hash della password dell'utente (uso interno del service).
     *
     * <p>In pratica, questo valore viene calcolato nel service layer usando
     * BCryptPasswordEncoder, prima di passarlo al builder.</p>
     *
     * @param passwordHash l'hash della password (calcolato con BCrypt)
     * @return this (per concatenamento fluente)
     */
    public UserBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    /**
     * Recupera la password in chiaro (per uso interno nel service layer).
     *
     * @return la password in chiaro
     */
    public String getRawPassword() {
        return rawPassword;
    }

    /**
     * Imposta il ruolo dell'utente.
     *
     * <p>Se non specificato, il default sarà Role.UTENTE.</p>
     *
     * @param role il ruolo (ADMIN o UTENTE)
     * @return this (per concatenamento fluente)
     */
    public UserBuilder withRole(Role role) {
        this.role = role;
        return this;
    }

    /**
     * Valida l'utente e lo restituisce pronto per essere persistito.
     *
     * <p>Controlli effettuati:</p>
     * <ul>
     *   <li>Username è specificato e non vuoto.</li>
     *   <li>Email è specificata e contiene il carattere '@' (validazione minima).</li>
     *   <li>Password hash è specificato.</li>
     *   <li>Username ha una lunghezza ragionevole (3-50 caratteri).</li>
     * </ul>
     *
     * @return l'entità User validata e pronta per il salvataggio
     * @throws IllegalArgumentException se uno dei campi obbligatori non è valido
     */
    public User build() {
        // Validazione dell'username
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("L'username è obbligatorio e non può essere vuoto.");
        }

        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException(
                    "L'username deve essere tra 3 e 50 caratteri. "
                    + "Lunghezza attuale: " + username.length()
            );
        }

        // Validazione dell'email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email è obbligatoria e non può essere vuota.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("L'email non è in un formato valido: " + email);
        }

        // Validazione del password hash
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("L'hash della password è obbligatorio.");
        }

        // Assegnazione del ruolo di default se non specificato
        if (role == null) {
            role = Role.UTENTE;
        }

        // Creazione dell'entità User
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        // Inizializzazione degli insiemi di follower e following
        user.setFollowing(new HashSet<>());
        user.setFollowers(new HashSet<>());

        return user;
    }
}
