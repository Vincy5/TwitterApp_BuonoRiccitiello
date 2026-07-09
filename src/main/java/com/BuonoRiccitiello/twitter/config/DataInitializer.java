package com.BuonoRiccitiello.twitter.config;

import com.BuonoRiccitiello.twitter.builder.UserBuilder;
import com.BuonoRiccitiello.twitter.model.Role;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import com.BuonoRiccitiello.twitter.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Crea un utente amministratore di default all'avvio,
 * utile per testare la sezione admin del progetto.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, AuthService authService) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new UserBuilder()
                        .withUsername("admin")
                        .withEmail("admin@twitterapp.it")
                        .withPasswordHash(authService.encodePassword("admin123"))
                        .withRole(Role.ADMIN)
                        .build()
                );
            }
        };
    }
}