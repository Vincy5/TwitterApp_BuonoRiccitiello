package com.BuonoRiccitiello.twitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configurazione Spring per i bean di sicurezza.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Definire il BCryptPasswordEncoder come bean Spring.</li>
 *   <li>Configurare altri bean di sicurezza se necessario.</li>
 * </ul>
 *
 * <p><strong>Nota:</strong></p>
 * <p>In un'applicazione reale con Spring Security completo, questo file conterrebbe
 * anche la configurazione della catena di filtri, session management, CSRF protection, ecc.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Crea il bean di BCryptPasswordEncoder.
     *
     * <p>BCrypt è un algoritmo di hashing delle password resistente al brute force.
     * Il parametro di forza (strength) è impostato di default a 10.</p>
     *
     * @return l'istanza di BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
