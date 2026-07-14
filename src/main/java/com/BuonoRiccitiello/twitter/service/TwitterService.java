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
 * Servizio core per l'orchestrazione della business logic dell'applicazione Twitter.
 * <p>
 * Questa classe funge da facciata e coordinatore principale del backend. Non implementa direttamente
 * i pattern architetturali ma ne orchestra l'uso per garantire il Single Responsibility Principle (SRP).
 * </p>
 *
 * <p><strong>Design Pattern Utilizzati:</strong></p>
 * <ul>
 *   <li><b>Builder:</b> Incapsula la complessa logica di creazione e validazione di {@link User} e {@link Message}.</li>
 *   <li><b>Factory Method:</b> Genera in modo polimorfico il canale di invio appropriato (SMS, Email, ecc.) tramite {@link ChannelFactory}.</li>
 *   <li><b>Observer:</b> Distribuisce in tempo reale le notifiche ai follower all'atto della pubblicazione di un messaggio.</li>
 *   <li><b>Command:</b> Isola ed esegue le operazioni di amministrazione (es. cancellazione utenti) tramite un invoker dedicato.</li>
 * </ul>
 *
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
     * Costruttore per l'iniezione delle dipendenze e l'inizializzazione del sistema di notifiche.
     * <p>
     * In fase di avvio, provvede ad agganciare gli osservatori globali (Log e Persistenza) al
     * registro dei soggetti {@link UserSubject}.
     * </p>
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

        // Registrazione centralizzata degli Observer sul ciclo di vita dei messaggi
        this.userSubject.attach(this.logNotificationObserver);
        this.userSubject.attach(this.notificationPersistenceObserver);

        this.notificationRepository = notificationRepository;
        this.commandInvoker = commandInvoker;
    }

    /**
     * Sottopone a persistenza un nuovo utente nel sistema applicando l'hashing della password.
     *
     * @param userBuilder l'istanza configurata del costruttore contenente i dati grezzi dell'utente
     * @return l'entità {@link User} salvata sul database e completa di ID generato
     * @throws UserAlreadyExistsException se lo username o l'indirizzo email risultano già censiti
     * @throws IllegalArgumentException   se la password in chiaro risulta assente o non valida
     */
    @Transactional
    public User registerUser(UserBuilder userBuilder) throws UserAlreadyExistsException {
        logger.info("Registrazione nuovo utente in corso...");

        String rawPassword = userBuilder.getRawPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La password è obbligatoria per la registrazione.");
        }

        // Cifratura della password prima della finalizzazione dell'oggetto business
        String hashedPassword = authService.encodePassword(rawPassword);
        userBuilder.withPasswordHash(hashedPassword);

        // Generazione dell'istanza: attiva la catena di validazione interna del Builder
        User user = userBuilder.build();

        // Controlli di univocità sui vincoli unici di business
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Lo username '" + user.getUsername() + "' è già registrato nel sistema."
            );
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException(
                    "L'email '" + user.getEmail() + "' è già registrata nel sistema."
            );
        }

        user = userRepository.save(user);
        logger.info("Utente '{}' registrato con successo (ID={})", user.getUsername(), user.getId());

        // Assicura che l'observer sia attivo per il monitoraggio delle notifiche
        userSubject.attach(notificationPersistenceObserver);

        return user;
    }

    /**
     * Stabilisce una relazione di "inseguimento" (follow) asimmetrica tra due utenti.
     *
     * @param followerId l'identificativo dell'utente che avvia l'azione
     * @param followedId l'identificativo dell'utente target che riceverà il follower
     * @throws IllegalArgumentException se gli ID coincidono o se una delle due entità non esiste
     */
    @Transactional
    public void follow(Long followerId, Long followedId) {
        logger.info("L'utente {} sta seguendo l'utente {}", followerId, followedId);

        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Non puoi seguire te stesso.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower con ID " + followerId + " non trovato"));

        User followedUser = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException("Utente da seguire con ID " + followedId + " non trovato"));

        // Impedisce duplicazioni nella collezione relazionale ManyToMany
        boolean alreadyFollowing = follower.getFollowing()
                .stream()
                .anyMatch(user -> user.getId().equals(followedId));

        if (alreadyFollowing) {
            logger.info("L'utente '{}' segue già '{}'", follower.getUsername(), followedUser.getUsername());
            return;
        }

        // Aggiornamento bidirezionale della relazione in memoria
        follower.getFollowing().add(followedUser);
        followedUser.getFollowers().add(follower);

        userRepository.save(follower);
        logger.info("L'utente '{}' sta ora seguendo '{}'", follower.getUsername(), followedUser.getUsername());
    }

    /**
     * Rimuove la relazione di "inseguimento" (unfollow) tra due utenti.
     * <p>
     * Interviene su entrambi i lati della relazione ManyToMany per mantenere sincronizzato lo stato
     * del grafo delle entità prima del flush sul database.
     * </p>
     *
     * @param followerId l'identificativo dell'utente che revoca il follow
     * @param followedId l'identificativo dell'utente non più seguito
     */
    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        logger.info("L'utente {} vuole smettere di seguire l'utente {}", followerId, followedId);

        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Non puoi fare unfollow di te stesso.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower con ID " + followerId + " non trovato"));

        User followedUser = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException("Utente da non seguire più con ID " + followedId + " non trovato"));

        follower.getFollowing().removeIf(user -> user.getId().equals(followedId));
        followedUser.getFollowers().removeIf(user -> user.getId().equals(followerId));

        userRepository.save(follower);
        userRepository.save(followedUser);

        logger.info("L'utente '{}' non segue più '{}'", follower.getUsername(), followedUser.getUsername());
    }

    /**
     * Estrae un utente per ID all'interno di una transazione di sola lettura.
     *
     * @param userId l'ID da cercare
     * @return l'entità {@link User} trovata
     * @throws IllegalArgumentException se l'utente non esiste
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utente con ID " + userId + " non trovato"));
    }

    /**
     * Restituisce l'insieme di ID degli utenti seguiti, escludendo l'utente stesso.
     *
     * @param userId l'ID dell'utente di riferimento
     * @return un {@link Set} di identificativi numerici
     */
    @Transactional(readOnly = true)
    public Set<Long> getFollowingIds(Long userId) {
        User user = getUserById(userId);
        return user.getFollowing()
                .stream()
                .map(User::getId)
                .filter(id -> !userId.equals(id))
                .collect(Collectors.toSet());
    }

    /**
     * Calcola il numero totale di utenti seguiti.
     *
     * @param userId l'ID dell'utente di riferimento
     * @return il conteggio degli utenti seguiti
     */
    @Transactional(readOnly = true)
    public int getFollowingCount(Long userId) {
        return (int) getUserById(userId).getFollowing()
                .stream()
                .filter(user -> !userId.equals(user.getId()))
                .count();
    }

    /**
     * Calcola il numero totale di follower accreditati.
     *
     * @param userId l'ID dell'utente di riferimento
     * @return il conteggio dei follower
     */
    @Transactional(readOnly = true)
    public int getFollowersCount(Long userId) {
        return (int) getUserById(userId).getFollowers()
                .stream()
                .filter(user -> !userId.equals(user.getId()))
                .count();
    }

    /**
     * Interroga il database per contare quanti messaggi ha pubblicato un utente.
     *
     * @param userId l'ID dell'autore
     * @return il numero complessivo di messaggi pubblicati
     */
    @Transactional(readOnly = true)
    public long getPublishedMessagesCount(Long userId) {
        return messageRepository.countByAuthor_Id(userId);
    }

    /**
     * Genera la timeline personalizzata (feed) per la home page di un utente.
     * <p>
     * Il feed include aggregandoli sia i messaggi scritti in prima persona dall'utente,
     * sia i messaggi pubblicati da tutte le persone incluse nella sua lista dei seguiti.
     * </p>
     *
     * @param userId l'ID dell'utente che richiede la timeline
     * @return la lista di {@link Message} ordinati dal più recente
     */
    @Transactional(readOnly = true)
    public List<Message> getFeedMessages(Long userId) {
        User user = getUserById(userId);
        List<Long> authorIds = new ArrayList<>();

        authorIds.add(user.getId());

        user.getFollowing().stream()
                .map(User::getId)
                .filter(id -> !userId.equals(id))
                .forEach(authorIds::add);

        return messageRepository.findByAuthor_IdInOrderByCreatedAtDesc(authorIds);
    }

    /**
     * Recupera l'elenco dei messaggi pubblicati esclusivamente dall'utente specificato.
     *
     * @param userId l'ID dell'autore
     * @return la lista cronologica decrescente dei messaggi personali
     */
    @Transactional(readOnly = true)
    public List<Message> getOwnMessages(Long userId) {
        return messageRepository.findByAuthor_IdOrderByCreatedAtDesc(userId);
    }

    /**
     * Estrae la lista degli utenti seguiti ordinandoli alfabeticamente per username.
     *
     * @param userId l'ID dell'utente di riferimento
     * @return la lista ordinata di tipo {@link User}
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
     * Estrae la lista dei follower di un utente ordinandoli alfabeticamente per username.
     *
     * @param userId l'ID dell'utente di riferimento
     * @return la lista ordinata dei follower
     */
    @Transactional(readOnly = true)
    public List<User> getFollowersUsers(Long userId) {
        return getUserById(userId).getFollowers()
                .stream()
                .filter(user -> !userId.equals(user.getId()))
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Conta le notifiche non lette presenti per un utente.
     *
     * @param userId l'ID dell'utente destinatario
     * @return il numero di notifiche pendenti
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationsCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndReadFalse(userId);
    }

    /**
     * Modifica la password di sicurezza dell'utente previa convalida della password corrente.
     *
     * @param userId          l'ID dell'utente richiedente
     * @param currentPassword la password attuale in chiaro da verificare
     * @param newPassword     la nuova credenziale da impostare (minimo 6 caratteri)
     * @param confirmPassword la stringa di controllo per la verifica di battitura della nuova password
     * @throws IllegalArgumentException se uno dei controlli di coerenza o validità fallisce
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
     * Gestisce il caricamento del file d'immagine delegando la persistenza al driver
     * di storage e aggiornando il record dell'utente con l'URL virtuale restituito.
     *
     * @param userId l'ID dell'utente proprietario del profilo
     * @param avatar il file binario multimediale proveniente dal client
     * @throws IllegalStateException se si riscontrano eccezioni bloccanti di I/O su disco
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
     * Consente la rimozione fisica di un messaggio assicurando preventivamente
     * il diritto di proprietà da parte dell'attore loggato.
     *
     * @param userId    l'ID dell'utente che impartisce il comando di cancellazione
     * @param messageId l'ID del messaggio da rimuovere dal database
     * @throws IllegalArgumentException se il messaggio non esiste o se l'autore non coincide con l'utente loggato
     */
    @Transactional
    public void deleteOwnMessage(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Messaggio con ID " + messageId + " non trovato"));

        if (message.getAuthor() == null || !message.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("Puoi eliminare solo i messaggi pubblicati da te.");
        }

        messageRepository.delete(message);
        logger.info("Messaggio ID={} eliminato dall'utente ID={}", messageId, userId);
    }

    /**
     * Esegue il ciclo completo di composizione, persistenza, instradamento e notifica di un nuovo post.
     * <p>
     * Questa procedura applica in sequenza:
     * 1. Il recupero dell'anagrafica autore.
     * 2. L'indicizzazione/creazione automatica dell'{@link Hashtag}.
     * 3. La strutturazione del messaggio tramite {@link MessageBuilder}.
     * 4. Il buffering sul canale di notifica tramite il pattern <b>Factory Method</b>.
     * 5. Il rilascio dell'evento globale verso i follower tramite l'infrastruttura <b>Observer</b>.
     * </p>
     *
     * @param authorId    l'ID dell'utente autore del post
     * @param content     il testo del messaggio (soggetto alle regole di validazione del Builder)
     * @param hashtagName la stringa dell'hashtag da associare (senza simbolo '#', opzionale)
     * @param channel     la tipologia enumerativa del canale sorgente dell'invio
     * @return il {@link Message} memorizzato ed elaborato con successo
     */
    @Transactional
    public Message postMessage(Long authorId, String content, String hashtagName, Channel channel) {
        logger.info("Pubblicazione messaggio da autore ID={} via {}", authorId, channel);

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Autore con ID " + authorId + " non trovato"));

        Hashtag hashtag = null;
        if (hashtagName != null && !hashtagName.trim().isEmpty()) {
            hashtag = hashtagRepository.findByName(hashtagName)
                    .orElseGet(() -> {
                        Hashtag newHashtag = new Hashtag();
                        newHashtag.setName(hashtagName);
                        return hashtagRepository.save(newHashtag);
                    });
        }

        Message message = new MessageBuilder()
                .withAuthor(author)
                .withContent(content)
                .withHashtag(hashtag)
                .withChannel(channel)
                .build();

        message = messageRepository.save(message);
        logger.info("Messaggio ID={} pubblicato da '{}' (lunghezza: {})",
                message.getId(), author.getUsername(), content.length());

        // Factory Method: Istanziazione polimorfa e invio sul canale logico corretto
        MessageChannel messageChannel = channelFactory.createChannel(channel);
        messageChannel.send(message);

        // Observer Pattern: Notifica asincrona e disaccoppiata della rete di follower
        userSubject.notifyNewMessage(message);

        return message;
    }

    /**
     * Esegue l'eliminazione amministrativa totale di un account utente e delle sue dipendenze.
     * <p>
     * Sfrutta il pattern <b>Command</b> per incapsulare l'intera sequenza distruttiva in un'unità
     * di lavoro atomica, gestita dall'invoker di sistema.
     * </p>
     *
     * @param userId l'ID dell'utente da estirpare dal sistema
     * @throws IllegalStateException se l'esecuzione del comando restituisce un esito fallimentare
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
     * Interroga l'archivio storico dei messaggi filtrandoli per hashtag specifico.
     * <p>
     * Delega l'estrazione dati a un oggetto comando specializzato del pattern <b>Command</b>.
     * </p>
     *
     * @param hashtagName la stringa testuale del tag da ricercare
     * @return la {@link List} di messaggi associati, oppure una lista vuota in caso di anomalie di cast
     */
    @SuppressWarnings("unchecked")
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
     * Estrae la totalità degli utenti registrati all'applicazione senza filtri.
     *
     * @return l'elenco completo degli utenti
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Esegue una ricerca puntuale basata sulla stringa dello username.
     *
     * @param username lo username da cercare
     * @return l'oggetto {@link User} se presente, altrimenti {@code null}
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}