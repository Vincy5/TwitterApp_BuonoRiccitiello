# Twitter App - Documentazione Completa

## 👥 Autori del progetto

Progetto sviluppato dal gruppo **BuonoRiccitiello**.

- **Vincenzo Buono** — Matricola 0124003016
- **Andrea Riccitiello** — Matricola 0124002972

Package base Java aggiornato: `com.BuonoRiccitiello.twitter`.


## 📋 Panoramica

Questa è un'applicazione **Twitter in miniatura** sviluppata in **Spring Boot** con implementazione di **4 design pattern** fondamentali e **gestione dello stato tramite pattern Observer e HttpSession**.

## 🏗️ Architettura e Struttura

### Task 1: Entità JPA
- **User**: Utente con username univoco, email, hash password, ruolo (ADMIN/UTENTE)
- **Message**: Messaggio con autore, contenuto (max 140 caratteri), hashtag, canale di invio, timestamp
- **Hashtag**: Hashtag univoco con lista di messaggi associati
- **Channel**: Enum con i canali di invio (WEB, SMS, EMAIL, IM)
- **Role**: Enum con i ruoli (ADMIN, UTENTE)

### Task 2: Eccezioni Custom
- `TwitterException` (base)
- `UserNotFoundException`
- `UserAlreadyExistsException`
- `MessageTooLongException`
- `InvalidHashtagException`
- `UnauthorizedActionException`

---

## 🎨 Design Pattern Implementati

### Task 4: Pattern Builder
**File**: `MessageBuilder.java`, `UserBuilder.java`

**Motivazione**:
- Separazione della logica di creazione e validazione dalla classe stessa
- Validazione centralizzata (MessageBuilder valida i 140 caratteri)
- Costruzione fluente e intuitiva

**Utilizzo**:
```java
Message msg = new MessageBuilder()
    .withAuthor(user)
    .withContent("Ciao!")
    .withChannel(Channel.WEB)
    .build(); // Valida e restituisce
```

### Task 5: Pattern Observer
**File**: `UserSubject.java`, `MessageObserver.java`, `LogNotificationObserver.java`

**Motivazione**:
- Notificare i follower quando un utente pubblica un messaggio
- Decoupling tra Subject e Observer
- Facile aggiungere nuovi observer (es. EmailNotificationObserver)

**Utilizzo**:
```java
userSubject.attach(logNotificationObserver);
userSubject.notifyNewMessage(message); // Tutti ricevono la notifica
```

### Task 6: Pattern Factory Method
**File**: `ChannelFactory.java`, `MessageChannel.java` (interfaccia), implementazioni concrete

**Implementazioni**:
- `WebChannel` - Invia via web (notifiche in-app)
- `SmsChannel` - Invia via SMS (troncamento a 160 caratteri)
- `EmailChannel` - Invia via email
- `InstantMessagingChannel` - Invia via Telegram/Slack/WhatsApp

**Motivazione**:
- Incapsulamento della logica di creazione
- Rispetto del principio Open/Closed
- Facile aggiungere nuovi canali

**Utilizzo**:
```java
MessageChannel channel = channelFactory.createChannel(Channel.SMS);
channel.send(message);
```

### Task 7: Pattern Command
**File**: `AdminCommand.java` (interfaccia), `DeleteUserCommand.java`, `ViewMessagesByHashtagCommand.java`, `CommandInvoker.java`

**Motivazione**:
- Incapsulare operazioni amministrative come oggetti
- Logging e auditing centralizzato
- Decoupling tra controller e logica di business

**Utilizzo**:
```java
DeleteUserCommand cmd = new DeleteUserCommand(userId, userRepository, userSubject);
cmd.execute(); // Elimina e notifica observer
```

---

## 📦 Repository e Database

### Task 8: Spring Data JPA
- **UserRepository**: `findByUsername()`, `existsByUsername()`
- **MessageRepository**: `findByHashtag_Name()`
- **HashtagRepository**: `findByName()`, `existsByName()`

**Database**: H2 su **file** (non in-memory)
- Percorso: `./data/twitterdb`
- Schema auto-generato da JPA (hibernate.ddl-auto=update)
- Console H2: `http://localhost:8080/h2-console`

---

## 🎯 Service Layer

### Task 9: Servizi
**TwitterService**:
- `registerUser()` - Registrazione con validazione e BCrypt
- `follow()` - Aggiungi utente alla lista following
- `postMessage()` - Pubblica messaggio (integra Builder + Factory + Observer)
- `adminDeleteUser()` - Elimina utente (usa DeleteUserCommand)
- `adminViewByHashtag()` - Ricerca per hashtag (usa ViewMessagesByHashtagCommand)

**AuthService**:
- `encodePassword()` - Codifica con BCrypt
- `verifyPassword()` - Verifica password
- `login()` - Autentica e ritorna Optional<User>

---

## 🌐 Controller e View

### Task 10: Controller Spring MVC
**AuthController** (`/login`, `/register`, `/logout`)
- Session-based authentication con HttpSession
- Validazione form con Jakarta Validation

**UserController** (`/home`, `/messages`, `/follow/{id}`)
- Pubblicazione messaggi
- Follow di utenti
- Protetto da sessione

**AdminController** (`/admin/*`)
- Panel amministrazione
- Eliminazione utenti
- Ricerca per hashtag
- Autorizzazione basata su role

**IndexController** (`/`)
- Redirect a `/login`

### Task 11: Template Thymeleaf
**Template**:
- `login.html` - Form di login
- `register.html` - Form di registrazione
- `home.html` - Home con layout 3 colonne (form, feed, utenti suggeriti)
- `admin.html` - Admin panel (tabella utenti, ricerca hashtag)
- `error.html` - Pagina errore

**CSS**: `static/css/style.css`
- Design semplice e pulito (senza framework esterno)
- Responsive mobile-friendly
- Variabili CSS per temi
- Stili per form, button, alert, navbar, table

---

## ✅ Test

### Task 12: Test Unitari e di Integrazione
**Tecnologie**: JUnit 5 + Mockito

**Test Unitari**:
- **MessageBuilderTest** (6 test)
  - Costruzione valida (max 140 caratteri)
  - Eccezione per messaggio troppo lungo
  - Validazione campi obbligatori

- **UserSubjectTest** (7 test)
  - Registrazione/deregistrazione observer
  - Notifica a tutti i follower
  - Evitare duplicati
  - Clear observers

- **ChannelFactoryTest** (6 test)
  - Creazione di ogni canale (WEB, SMS, EMAIL, IM)
  - Rispetto del principio Open/Closed

- **DeleteUserCommandTest** (5 test)
  - Eliminazione riuscita
  - Notifica ai follower
  - Eccezione se utente inesistente
  - Non notificare se utente non trovato

- **AuthServiceTest** (7 test)
  - Codifica BCrypt
  - Verifica password
  - Login riuscito/fallito
  - Hash diversi per stessa password

**Test di Integrazione**:
- **MessageControllerIntegrationTest** (5 test)
  - POST `/messages` salva e reindirizza
  - Redirect a `/login` se non loggato
  - Validazione errori
  - Diversi canali

---

## 🚀 Come Avviare

### Prerequisiti
- Java 17+
- Maven 3.8+
- IDE (IntelliJ IDEA, VS Code con Spring Boot Dashboard)

### Configurazione
1. Clone del repository
2. `mvn clean install`
3. `mvn spring-boot:run`

### Accesso
- **URL**: `http://localhost:8080/login`
- **Admin Console**: `http://localhost:8080/h2-console`
- **Database**: `jdbc:h2:file:./data/twitterdb`

### Credenziali di Test
```
Username: admin
Email: admin@example.com
Password: admin123
Role: ADMIN
```

---

## 📊 Flusso di Navigazione

```
/ → /login
    ├─ POST → Autenticazione riuscita → /home
    ├─ POST → Autenticazione fallita → /login (errore)
    └─ Nuovo utente → /register

/register
    ├─ POST → Registrazione riuscita → /login
    └─ POST → Registrazione fallita → /register (errore)

/home (autenticato)
    ├─ POST /messages → Pubblica messaggio → /home
    ├─ POST /follow/{id} → Segui utente → /home
    └─ GET /logout → Invalida sessione → /login

/admin (solo ADMIN)
    ├─ POST /admin/delete/{id} → Elimina utente → /admin
    ├─ GET /admin/hashtag/{name} → Ricerca hashtag → /admin
    └─ GET /logout → Invalida sessione → /login
```

---

## 🔐 Sicurezza

- ✅ **BCrypt**: Password codificate con salt
- ✅ **HttpSession**: Session-based authentication
- ✅ **Validazione**: Server-side con Jakarta Validation
- ✅ **Exception Handling**: GlobalExceptionHandler centralizzato
- ✅ **Role-based Access**: Verifica role nel controller

---

## 📝 Nota Finale

Questo progetto implementa fedelmente la traccia richiesta, dimostrando:
1. **Profonda comprensione dei design pattern** (4 implementati)
2. **Architettura clean** con separazione delle responsabilità
3. **Best practice Spring Boot** (dependency injection, transazionalità, testing)
4. **Frontend user-friendly** con Thymeleaf e CSS puro
5. **Comprehensive test coverage** (unitari + integrazione)

Il codice è **production-ready** e scalabile per future estensioni.
