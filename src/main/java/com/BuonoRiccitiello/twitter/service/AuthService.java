package com.BuonoRiccitiello.twitter.service;

import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servizio per l'autenticazione degli utenti.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Gestire il login degli utenti (verifica username e password).</li>
 *   <li>Codificare le password con BCrypt al momento della registrazione.</li>
 *   <li>Verificare le password al momento del login.</li>
 * </ul>
 *
 * <p><strong>Note sulla sicurezza:</strong></p>
 * <ul>
 *   <li>Le password non sono mai memorizzate in chiaro.</li>
 *   <li>BCrypt include salt e è resistente al brute force.</li>
 * </ul>
 *
 * <p><strong>Utilizzo:</strong></p>
 * <pre>
 *   String hashedPassword = authService.encodePassword("miaPassword123");
 *   boolean isValid = authService.verifyPassword("miaPassword123", hashedPassword);
 *   Optional&lt;User&gt; user = authService.login("username", "password");
 * </pre>
 */
@Service
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * Costruttore di AuthService.
     *
     * @param passwordEncoder il BCryptPasswordEncoder bean configurato in Spring
     * @param userRepository il repository per accedere agli utenti
     */
    public AuthService(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Codifica una password in plain text usando BCrypt.
     *
     * <p>Questo metodo è chiamato durante la registrazione di un nuovo utente
     * prima di salvare l'utente nel database.</p>
     *
     * @param rawPassword la password in chiaro
     * @return l'hash BCrypt della password
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifica che una password in chiaro corrisponda a un hash BCrypt.
     *
     * <p>Questo metodo è chiamato durante il login per verificare che la password
     * fornita dall'utente corrisponda all'hash salvato nel database.</p>
     *
     * @param rawPassword la password in chiaro fornita dall'utente
     * @param hashedPassword l'hash BCrypt salvato nel database
     * @return true se le password corrispondono, false altrimenti
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Autentica un utente verificando le sue credenziali.
     *
     * <p><strong>Processo:</strong></p>
     * <ol>
     *   <li>Ricerca l'utente per username nel database.</li>
     *   <li>Se non trovato, ritorna Optional.empty().</li>
     *   <li>Se trovato, verifica la password.</li>
     *   <li>Ritorna l'utente se la password è corretta, Optional.empty() altrimenti.</li>
     * </ol>
     *
     * @param username lo username dell'utente
     * @param rawPassword la password in chiaro fornita dall'utente
     * @return Optional contenente l'utente se l'autenticazione ha successo,
     *         Optional.empty() altrimenti
     */
    public Optional<User> login(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> verifyPassword(rawPassword, user.getPasswordHash()));
    }
}
