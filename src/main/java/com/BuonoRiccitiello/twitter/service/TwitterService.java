package com.BuonoRiccitiello.twitter.service;

import com.BuonoRiccitiello.twitter.builder.MessageBuilder;
import com.BuonoRiccitiello.twitter.builder.UserBuilder;
import com.BuonoRiccitiello.twitter.command.CommandInvoker;
import com.BuonoRiccitiello.twitter.command.DeleteUserCommand;
import com.BuonoRiccitiello.twitter.command.ViewMessagesByHashtagCommand;
import com.BuonoRiccitiello.twitter.exception.UserAlreadyExistsException;
import com.BuonoRiccitiello.twitter.factory.ChannelFactory;
import com.BuonoRiccitiello.twitter.factory.MessageChannel;
import com.BuonoRiccitiello.twitter.model.Channel;
import com.BuonoRiccitiello.twitter.model.Hashtag;
import com.BuonoRiccitiello.twitter.model.Message;
import com.BuonoRiccitiello.twitter.model.User;
import com.BuonoRiccitiello.twitter.observer.LogNotificationObserver;
import com.BuonoRiccitiello.twitter.observer.NotificationPersistenceObserver;
import com.BuonoRiccitiello.twitter.observer.UserSubject;
import com.BuonoRiccitiello.twitter.repository.HashtagRepository;
import com.BuonoRiccitiello.twitter.repository.MessageRepository;
import com.BuonoRiccitiello.twitter.repository.UserRepository;
import com.BuonoRiccitiello.twitter.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import com.BuonoRiccitiello.twitter.storage.AvatarStorage;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Servizio principale per l'orchestrazione della logica di business dell'applicazione Twitter.
 *
 * <p><strong>Responsabilità:</strong></p>
 * <ul>
 *   <li>Gestire la registrazione e il follow degli utenti.</li>
 *   <li>Gestire la pubblicazione dei messaggi con i pattern Builder e Factory.</li>
 *   <li>Notificare i follower tramite il pattern Observer.</li>
 *   <li>Orchestrare le operazioni amministrative tramite il pattern Command.</li>
 * </ul>
 *
 * <p><strong>Design Pattern utilizzati:</strong></p>
 * <ul>
 *   <li><strong>Builder:</strong> Creazione di User e Message con validazione.</li>
 *   <li><strong>Factory Method:</strong> Creazione dei canali di invio messaggi.</li>
 *   <li><strong>Observer:</strong> Notificazione dei follower.</li>
 *   <li><strong>Command:</strong> Esecuzione di operazioni amministrative.</li>
 * </ul>
 *
 * <p><strong>Transazionalità:</strong></p>
 * <p>I metodi critici sono annotati con @Transactional per garantire la consistenza
 * dei dati e il rollback automatico in caso di errori.</p>
 *
 * <p><strong>Single Responsibility Principle:</strong></p>
 * <p>TwitterService NON reimplementa i pattern; li USA orchestrandoli.
 * La logica dei pattern rimane nei loro moduli specifici.</p>
 */
@Service
public class TwitterService {

    private static final Logger logger = LoggerFactory.getLogger(TwitterService.class);

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final HashtagRepository hashtagRepository;
    private final AuthService authService;
    private final ChannelFactory channelFactory;
    private final UserSubject userSubject;
    private final LogNotificationObserver logNotificationObserver;
    private final NotificationPersistenceObserver notificationPersistenceObserver;
    private final AvatarStorage avatarStorage;
    private final NotificationRepository notificationRepository;
    private final CommandInvoker commandInvoker;
    

    /**
     * Costruttore con dependency injection.
     */
    public TwitterService(
            UserRepository userRepository,
            MessageRepository messageRepository,
            HashtagRepository hashtagRepository,
            AuthService authService,
            ChannelFactory channelFactory,
            UserSubject userSubject,
            LogNotificationObserver logNotificationObserver,
            NotificationPersistenceObserver notificationPersistenceObserver,
            AvatarStorage avatarStorage,
            NotificationRepository notificationRepository,
            CommandInvoker commandInvoker
    ) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.hashtagRepository = hashtagRepository;
        this.authService = authService;
        this.channelFactory = channelFactory;
        this.userSubject = userSubject;
        this.logNotificationObserver = logNotificationObserver;
        this.notificationPersistenceObserver = notificationPersistenceObserver;
        this.avatarStorage = avatarStorage;
        this.userSubject.attach(this.logNotificationObserver);
        this.userSubject.attach(this.notificationPersistenceObserver);
        this.notificationRepository = notificationRepository;
        this.commandInvoker = commandInvoker;
    }

    /**
     * Registra un nuovo utente nel sistema.
     *
     * <p><strong>Processo:</strong></p>
     * <ol>
     *   <li>Verifica che lo username non sia già registrato.</li>
     *   <li>Codifica la password con BCrypt.</li>
     *   <li>Costruisce l'utente tramite UserBuilder (validazione centralizzata).</li>
     *   <li>Salva l'utente nel database.</li>
     *   <li>Registra l'observer per le notifiche.</li>
     * </ol>
     *
     * @param userBuilder il builder configurato con i dati dell'utente
     * @return l'utente appena registrato
     * @throws UserAlreadyExistsException se lo username è già in uso
     * @throws IllegalArgumentException se i dati dell'utente non sono validi
     */
    @Transactional
    public User registerUser(UserBuilder userBuilder) throws UserAlreadyExistsException {
        logger.info("Registrazione nuovo utente in corso...");

        // Recupera la password in chiaro dal builder
        String rawPassword = userBuilder.getRawPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La password è obbligatoria per la registrazione.");
        }

        // Codifica la password con BCrypt
        String hashedPassword = authService.encodePassword(rawPassword);
        userBuilder.withPasswordHash(hashedPassword);

        // Costruisce e valida l'utente (questo farà lanciare eccezioni se i dati non sono validi)
        User user = userBuilder.build();

        // Verifica che lo username non esista già (dopo la build per coerenza)
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Lo username '" + user.getUsername() + "' è già registrato nel sistema."
            );
        }

        // Verifica che la email non esista già (dopo la build per coerenza)
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException(
                    "L'email '" + user.getEmail() + "' è già registrata nel sistema."
            );
        }

        // Salva nel database
        user = userRepository.save(user);
        logger.info("Utente '{}' registrato con successo (ID={})", user.getUsername(), user.getId());

        // Gli observer vengono ora registrati nel costruttore del service
        // in modo che siano attivi anche per gli utenti già presenti nel DB.
        userSubject.attach(notificationPersistenceObserver);

        return user;
    }

    /**
     * Un utente segue un altro utente.
     *
     * <p>Aggiunge l'utente seguito alla lista di "following" dell'utente follower.</p>
     *
     * @param followerId l'ID dell'utente che vuole seguire
     * @param followedId l'ID dell'utente da seguire
     * @throws IllegalArgumentException se uno degli utenti non esiste
     */
    @Transactional
    public void follow(Long followerId, Long followedId) {
        logger.info("L'utente {} sta seguendo l'utente {}", followerId, followedId);

        if (followerId.equals(followedId)) {
                throw new IllegalArgumentException("Non puoi seguire te stesso.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Follower con ID " + followerId + " non trovato"
                ));

        User followedUser = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utente da seguire con ID " + followedId + " non trovato"
                ));

        boolean alreadyFollowing = follower.getFollowing()
                .stream()
                .anyMatch(user -> user.getId().equals(followedId));

        if (alreadyFollowing) {
                logger.info("L'utente '{}' segue già '{}'",
                        follower.getUsername(), followedUser.getUsername());
                return;
        }

        follower.getFollowing().add(followedUser);
        followedUser.getFollowers().add(follower);

        userRepository.save(follower);

        logger.info("L'utente '{}' sta ora seguendo '{}'",
                follower.getUsername(), followedUser.getUsername());
 }


    /**
     * Smette di seguire un utente.
     *
     * <p>Rimuove la relazione ManyToMany sia dal lato following sia dal lato followers,
     * in modo da mantenere coerente il database.</p>
     *
     * @param followerId l'ID dell'utente che vuole smettere di seguire
     * @param followedId l'ID dell'utente da non seguire più
     */
    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        logger.info("L'utente {} vuole smettere di seguire l'utente {}", followerId, followedId);

        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Non puoi fare unfollow di te stesso.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Follower con ID " + followerId + " non trovato"
                ));

        User followedUser = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utente da non seguire più con ID " + followedId + " non trovato"
                ));

        follower.getFollowing().removeIf(user -> user.getId().equals(followedId));
        followedUser.getFollowers().removeIf(user -> user.getId().equals(followerId));

        userRepository.save(follower);
        userRepository.save(followedUser);

        logger.info("L'utente '{}' non segue più '{}'",
                follower.getUsername(), followedUser.getUsername());
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utente con ID " + userId + " non trovato"
                ));
    }

    @Transactional(readOnly = true)
    public Set<Long> getFollowingIds(Long userId) {
        User user = getUserById(userId);

        return user.getFollowing()
                .stream()
                .map(User::getId)
                .filter(id -> !userId.equals(id))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public int getFollowingCount(Long userId) {
        return (int) getUserById(userId).getFollowing()
                .stream()
                .filter(user -> !userId.equals(user.getId()))
                .count();
    }

    @Transactional(readOnly = true)
    public int getFollowersCount(Long userId) {
        return (int) getUserById(userId).getFollowers()
                .stream()
                .filter(user -> !userId.equals(user.getId()))
                .count();
    }

    @Transactional(readOnly = true)
    public long getPublishedMessagesCount(Long userId) {
        return messageRepository.countByAuthor_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<Message> getFeedMessages(Long userId) {
        User user = getUserById(userId);

        List<Long> authorIds = new ArrayList<>();

        // Mostro anche i messaggi dell'utente loggato
        authorIds.add(user.getId());

        // Mostro i messaggi degli utenti che lui segue
        user.getFollowing().stream()
                .map(User::getId)
                .filter(id -> !userId.equals(id))
                .forEach(authorIds::add);

        return messageRepository.findByAuthor_IdInOrderByCreatedAtDesc(authorIds);
    }

    /**
     * Recupera i messaggi pubblicati dall'utente loggato, dal più recente al meno recente.
     *
     * @param userId ID dell'utente
     * @return lista dei messaggi personali
     */
    @Transactional(readOnly = true)
    public List<Message> getOwnMessages(Long userId) {
        return messageRepository.findByAuthor_IdOrderByCreatedAtDesc(userId);
    }

    /**
     * Recupera gli utenti seguiti dall'utente indicato.
     *
     * @param userId ID dell'utente
     * @return lista degli utenti seguiti ordinata per username
     */
    @Transactional(readOnly = true)
    public List<User> getFollowingUsers(Long userId) {
        return getUserById(userId).getFollowing()
                .stream()
                .filter(user -> !userId.equals(user.getId()))
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Recupera gli utenti che seguono l'utente indicato.
     *
     * @param userId ID dell'utente
     * @return lista dei follower ordinata per username
     */
    @Transactional(readOnly = true)
    public List<User> getFollowersUsers(Long userId) {
        return getUserById(userId).getFollowers()
                .stream()
                .filter(user -> !userId.equals(user.getId()))
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationsCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndReadFalse(userId);
    }

    /**
     * Aggiorna la password dell'utente dopo aver verificato quella attuale.
     *
     * @param userId ID dell'utente loggato
     * @param currentPassword password attuale
     * @param newPassword nuova password
     * @param confirmPassword conferma nuova password
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = getUserById(userId);

        if (!authService.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("La password attuale non è corretta.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("La nuova password deve avere almeno 6 caratteri.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("La nuova password e la conferma non corrispondono.");
        }

        user.setPasswordHash(authService.encodePassword(newPassword));
        userRepository.save(user);
        logger.info("Password aggiornata per l'utente '{}'", user.getUsername());
    }

    /**
     * Salva l'immagine profilo dell'utente nella cartella uploads/avatars e memorizza
     * il percorso pubblico nel database.
     *
     * @param userId ID dell'utente
     * @param avatar file immagine caricato dal form
     */
    @Transactional
    public void updateProfileImage(Long userId, MultipartFile avatar) {
        User user = getUserById(userId);
        try {
            String publicPath = avatarStorage.storeAvatar(userId, avatar);
            user.setProfileImagePath(publicPath);
            userRepository.save(user);
            logger.info("Immagine profilo aggiornata per l'utente '{}'", user.getUsername());
        } catch (IOException e) {
            throw new IllegalStateException("Errore durante il salvataggio dell'immagine profilo.", e);
        }
    }

    /**
     * Elimina un messaggio pubblicato dall'utente loggato.
     *
     * <p>Il controllo sull'autore evita che un utente possa eliminare messaggi
     * pubblicati da altri utenti.</p>
     *
     * @param userId ID dell'utente loggato
     * @param messageId ID del messaggio da eliminare
     */
    @Transactional
    public void deleteOwnMessage(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Messaggio con ID " + messageId + " non trovato"
                ));

        if (message.getAuthor() == null || !message.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("Puoi eliminare solo i messaggi pubblicati da te.");
        }

        messageRepository.delete(message);
        logger.info("Messaggio ID={} eliminato dall'utente ID={}", messageId, userId);
    }

    /**
     * Pubblica un nuovo messaggio nel sistema.
     *
     * <p><strong>Processo:</strong></p>
     * <ol>
     *   <li>Trova l'autore del messaggio.</li>
     *   <li>Crea o recupera l'hashtag (se specificato).</li>
     *   <li>Costruisce il messaggio con MessageBuilder (validazione della lunghezza).</li>
     *   <li>Salva il messaggio nel database.</li>
     *   <li>Invia il messaggio tramite il canale specificato (Factory Method).</li>
     *   <li>Notifica i follower tramite Observer pattern.</li>
     * </ol>
     *
     * @param authorId l'ID dell'autore del messaggio
     * @param content il contenuto del messaggio (max 140 caratteri)
     * @param hashtagName il nome dell'hashtag (opzionale, può essere null)
     * @param channel il canale di invio (WEB, SMS, EMAIL, IM)
     * @return il messaggio pubblicato
     * @throws IllegalArgumentException se l'autore non esiste
     * @throws com.BuonoRiccitiello.twitter.exception.MessageTooLongException se il contenuto supera 140 caratteri
     */
    @Transactional
    public Message postMessage(Long authorId, String content, String hashtagName, Channel channel) {
        logger.info("Pubblicazione messaggio da autore ID={} via {}", authorId, channel);

        // 1. Trova l'autore
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Autore con ID " + authorId + " non trovato"
                ));

        // 2. Crea o recupera l'hashtag
        Hashtag hashtag = null;
        if (hashtagName != null && !hashtagName.trim().isEmpty()) {
            hashtag = hashtagRepository.findByName(hashtagName)
                    .orElseGet(() -> {
                        Hashtag newHashtag = new Hashtag();
                        newHashtag.setName(hashtagName);
                        return hashtagRepository.save(newHashtag);
                    });
        }

        // 3. Costruisce il messaggio con MessageBuilder (validazione inclusa)
        Message message = new MessageBuilder()
                .withAuthor(author)
                .withContent(content)
                .withHashtag(hashtag)
                .withChannel(channel)
                .build();

        // 4. Salva nel database
        message = messageRepository.save(message);
        logger.info("Messaggio ID={} pubblicato da '{}' (lunghezza: {})",
                message.getId(), author.getUsername(), content.length());

        // 5. Invia tramite il canale (Factory Method)
        MessageChannel messageChannel = channelFactory.createChannel(channel);
        messageChannel.send(message);

        // 6. Notifica i follower (Observer pattern)
        userSubject.notifyNewMessage(message);

        return message;
    }

    /**
     * Elimina un utente dal sistema (operazione amministrativa).
     *
     * <p>Utilizza il pattern Command per incapsulare l'operazione di eliminazione.</p>
     *
     * @param userId l'ID dell'utente da eliminare
     * @throws com.BuonoRiccitiello.twitter.exception.UserNotFoundException se l'utente non esiste
     */
    @Transactional
    public void adminDeleteUser(Long userId) {
        logger.info("Eliminazione amministrativa dell'utente ID={}", userId);

        DeleteUserCommand deleteCommand = new DeleteUserCommand(
                userId,
                userRepository,
                messageRepository,
                notificationRepository,
                userSubject
        );

        boolean success = commandInvoker.executeCommand(deleteCommand);

        if (!success) {
            throw new IllegalStateException("Errore durante l'eliminazione dell'utente.");
        }
    }

    /**
     * Recupera i messaggi filtrati per hashtag (operazione amministrativa).
     *
     * <p>Utilizza il pattern Command per incapsulare l'operazione di ricerca.</p>
     *
     * @param hashtagName il nome dell'hashtag da cercare
     * @return la lista di messaggi con l'hashtag specificato
     */
    @Transactional(readOnly = true)
    public List<Message> adminViewByHashtag(String hashtagName) {
        logger.info("Ricerca messaggi per hashtag: '{}'", hashtagName);

        ViewMessagesByHashtagCommand viewCommand = new ViewMessagesByHashtagCommand(
                hashtagName,
                messageRepository
        );

        Object result = commandInvoker.executeCommandAndGetResult(viewCommand);

        if (result instanceof List<?>) {
            return (List<Message>) result;
        }

        return List.of();
    }

    /**
     * Recupera tutti gli utenti del sistema.
     *
     * @return la lista di tutti gli utenti
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Recupera un utente per username.
     *
     * @param username lo username da cercare
     * @return l'utente se trovato, altrimenti un Optional vuoto
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
}
