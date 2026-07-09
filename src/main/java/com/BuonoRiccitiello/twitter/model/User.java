package com.BuonoRiccitiello.twitter.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entità che rappresenta un utente dell'applicazione.
 *
 * Contiene username univoco, email, hash della password, ruolo e relazioni
 * ManyToMany verso se stesso per follower/following.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"following", "followers"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Percorso pubblico dell'immagine profilo caricata dall'utente.
     * Se nullo, l'interfaccia usa l'avatar di default.
     */
    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Utenti che questo utente segue (owning side).
     */
    @ManyToMany
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<User> following = new HashSet<>();

    /**
     * Utenti che seguono questo utente (inverse side).
     */
    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();
}
