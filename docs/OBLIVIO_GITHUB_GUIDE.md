# Oblivio GitHub Guide

Schritt-für-Schritt-Anleitung durch das GitHub-Repository. Erklärt für jede Datei: **was sie macht**, **was angepasst wurde**, und **wie sie mit Railway und Supabase verbunden ist**.

> **Repository:** https://github.com/riccaden/promise
> **Diese Anleitung** ist die praktische Ergänzung zum `OBLIVIO_BUILD_GUIDE.md`. Wenn du das Repo geforked hast und Schritt für Schritt durchgehen willst, was wo steht — bist du hier richtig.

---

## Inhaltsverzeichnis

1. [Repo-Übersicht auf einen Blick](#1-repo-übersicht-auf-einen-blick)
2. [Wie Code von GitHub nach Railway kommt](#2-wie-code-von-github-nach-railway-kommt)
3. [Wie das System mit Supabase verbunden ist](#3-wie-das-system-mit-supabase-verbunden-ist)
4. [Die wichtigsten Dateien im Root](#4-die-wichtigsten-dateien-im-root)
5. [Der `src/`-Ordner — Java-Backend](#5-der-src-ordner--java-backend)
6. [Der `sql/`-Ordner — Datenbank-Skripte](#6-der-sql-ordner--datenbank-skripte)
7. [Der `docs/`-Ordner — Dokumentation](#7-der-docs-ordner--dokumentation)
8. [Was NICHT auf GitHub liegt](#8-was-nicht-auf-github-liegt)
9. [Workflow: Vom Code-Change zum Live-Deployment](#9-workflow-vom-code-change-zum-live-deployment)
10. [Anpassungen pro Datei — was und warum](#10-anpassungen-pro-datei--was-und-warum)

---

## 1. Repo-Übersicht auf einen Blick

Wenn du das Repo öffnest, siehst du zunächst diese Struktur:

```
oblivio-backend/                  ← Repo-Root
├── .mvn/                         Maven Wrapper Configuration
├── .readme/                      README-Assets (Bilder, etc.)
├── docs/                         Dokumentation
├── sql/                          Datenbank-Skripte
├── src/                          Java-Backend Quellcode
├── .env.example                  Vorlage für Umgebungsvariablen
├── .gitignore                    Welche Dateien Git ignoriert
├── .railwayignore                Welche Dateien Railway ignoriert
├── CITATION.cff                  Zitierungsmetadaten
├── Dockerfile                    Docker-Build-Anleitung
├── LICENSE                       Lizenzdatei
├── README.md                     Hauptdokumentation
├── mvnw, mvnw.cmd                Maven Wrapper Scripts
├── pom.xml                       Maven-Konfiguration (Dependencies)
└── railway.json                  Railway-Deployment-Konfiguration
```

**Wichtig:** Das **Frontend** (`Website/`) liegt NICHT in diesem Repo — es wird separat auf Hostpoint per FTP hochgeladen. Auf GitHub findest du nur das **Backend**.

---

## 2. Wie Code von GitHub nach Railway kommt

Das Setup ist ein klassisches Auto-Deploy-Modell:

```
   GitHub (oblivio-backend)
        │
        │  git push origin main
        ▼
   Railway erkennt den Push automatisch
        │
        │  Liest Dockerfile
        ▼
   Docker-Build startet:
   1. Stage 1: JDK + Maven → kompiliert JAR
   2. Stage 2: JRE + JAR → fertiges Image
        │
        ▼
   Container wird gestartet
        │
        │  Liest Env-Variablen aus Railway-Dashboard
        ▼
   Spring Boot Application läuft
        │
        ├─► PROMISE State Machine aktiv
        ├─► REST-API auf Port 8080
        ├─► Connection-Pool zu Supabase (HikariCP)
        └─► OpenAI + ElevenLabs Clients bereit
```

**Konkret:** Wenn du eine Java-Datei änderst, sie committest und mit `git push` hochlädst, baut Railway innerhalb von 2–5 Minuten ein neues Image und ersetzt den laufenden Container. Du musst nichts weiter tun.

---

## 3. Wie das System mit Supabase verbunden ist

Supabase ist die Datenbank im Hintergrund. Das Backend verbindet sich beim Start mit ihr — und zwar via **JDBC**, einer Standard-Datenbank-Schnittstelle für Java.

### Wo steht die Verbindung im Code?

Nicht im Quellcode direkt, sondern in den **Properties-Dateien** unter `src/main/resources/`:

**Datei `application-prod.properties`** (für Produktion auf Railway):
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

Die Werte `${...}` sind Platzhalter — sie werden von Railway aus den Environment-Variablen befüllt. So liegt das **Passwort nie im Repo**.

### Was passiert beim Start?

1. Spring Boot liest `application-prod.properties`
2. Spring liest die Env-Variablen von Railway
3. JDBC öffnet eine Verbindung zu Supabase PostgreSQL
4. Hibernate prüft, ob alle benötigten Tabellen existieren — wenn nicht, **erstellt** es sie automatisch (`ddl-auto=update`)
5. Backend ist bereit für Anfragen

### Welche Tabellen werden automatisch erstellt?

Hibernate erstellt aus den Java-Klassen mit `@Entity` automatisch Tabellen:
- `agent`, `state`, `prompt` (alle drei in einer Tabelle dank Inheritance)
- `transition`, `prompt_transitions`
- `utterance`, `utterances`
- `storage`, `storage_entry`

Diese liegen alle in der **Supabase-Datenbank** und werden vom Backend bei jedem Gespräch beschrieben.

### Was wird manuell in Supabase angelegt?

Die Oblivio-spezifischen Tabellen (für Frontend-Daten):
- `user_agents`
- `user_legacies`
- `legacy_access_codes`
- `legacy_messages`
- `questionnaire_answers`

Diese werden via `sql/SUPABASE_TABLES.sql` manuell angelegt — sie sind nicht durch Hibernate gemanagt, weil das Frontend direkt darauf zugreift (via Supabase JS Client).

---

## 4. Die wichtigsten Dateien im Root

### `README.md`

Die erste Datei, die jemand auf GitHub sieht. Enthält:
- Was Oblivio ist (Projektbeschreibung)
- Tech-Stack
- 8 Sprachen, 3 Varianten, 10 Blöcke
- Setup-Anleitung
- Link zu detaillierter Doku in `docs/`

**Angepasst:** Komplett neu geschrieben für Oblivio (im PROMISE-Original stand etwas anderes).

### `pom.xml`

Maven-Konfigurationsdatei. Definiert alle Dependencies und den Build-Prozess.

**Wichtige Einträge:**
```xml
<groupId>ch.zhaw</groupId>
<artifactId>statefulconversation</artifactId>
<version>0.0.1-SNAPSHOT</version>

<dependencies>
    <!-- Spring Boot Web → REST-API -->
    <dependency>spring-boot-starter-web</dependency>

    <!-- JPA + Hibernate → Datenbank-Mapping -->
    <dependency>spring-boot-starter-data-jpa</dependency>

    <!-- PostgreSQL-Treiber → für Supabase -->
    <dependency>org.postgresql:postgresql</dependency>

    <!-- Gson → JSON-Serialisierung -->
    <dependency>com.google.code.gson:gson</dependency>

    <!-- Actuator → Health-Check für Railway -->
    <dependency>spring-boot-starter-actuator</dependency>
</dependencies>
```

**Warum wichtig:** Wenn du eine neue Bibliothek nutzen willst (z.B. für TTS oder Auth), kommt sie hier rein. Railway baut beim nächsten Push automatisch mit den neuen Dependencies.

**Angepasst:** PostgreSQL-Driver hinzugefügt (für Supabase), MySQL-Driver entfernt (aus PROMISE-Original).

### `Dockerfile`

Die Anleitung, wie Railway das Image baut. Multi-Stage Build:

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
USER nobody
CMD ["java", "-jar", "app.jar"]
```

**Warum Multi-Stage?** Stage 1 hat das ganze JDK + Maven (~700 MB) — wir brauchen es nur zum Bauen. Stage 2 hat nur das JRE (~200 MB) — kleinere Production-Images.

**Angepasst:** Komplett neu geschrieben für Java 21 + Eclipse Temurin (im PROMISE-Original gab es kein Dockerfile).

### `railway.json`

Railway-spezifische Konfiguration. Sagt Railway, **wie der Container gestartet werden soll**.

```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "DOCKERFILE"
  },
  "deploy": {
    "startCommand": "java -jar app.jar",
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 100,
    "restartPolicyType": "ON_FAILURE"
  }
}
```

**Was passiert:**
- Railway baut mit Docker (nicht mit Nixpacks)
- Beim Start wird `java -jar app.jar` ausgeführt
- Railway prüft alle paar Sekunden `/actuator/health` — wenn das nicht antwortet, wird der Container neu gestartet
- Bei Fehlern wird automatisch neu gestartet

**Angepasst:** Komplett neu für Oblivio.

### `.railwayignore`

Welche Dateien Railway beim Build **ignorieren** soll. So sparen wir Build-Zeit und Image-Grösse.

```
target/         # Maven-Build-Output (wird auf Railway neu gebaut)
.git/           # Git-Historie braucht Railway nicht
docs/           # Dokumentation muss nicht ins Image
sql/            # SQL-Skripte gehören in die DB, nicht ins Image
*.md            # Markdown-Dateien sind nicht relevant
```

**Angepasst:** Neu für Oblivio.

### `.gitignore`

Welche Dateien Git **nicht versionieren** soll. Verhindert, dass sensible Daten und Build-Artefakte ins Repo gelangen.

```
# Build-Artefakte
target/
*.class

# IDE-Konfigurationen
.idea/
.vscode/
*.iml

# Sensible Daten (NIE im Repo!)
src/main/resources/application.properties
src/main/resources/openai.properties

# macOS
.DS_Store

# Persönliche/Thesis-Dateien
*.docx
Personas/
PROMISE_TECHNICAL_DOCUMENTATION.md
```

**Wichtig:** Die `application.properties` und `openai.properties` mit den lokalen API-Keys liegen NIE im Repo. Stattdessen gibt es Templates (`.template`-Endung), die im Repo sind.

**Angepasst:** Erweitert um Oblivio-spezifische Einträge (Personas/, *.docx etc.).

### `.env.example`

Vorlage für Environment-Variablen. Liegt im Repo als **Referenz**, nicht zur direkten Nutzung.

```bash
# Supabase Connection
SPRING_DATASOURCE_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-password-here

# OpenAI
OPENAI_KEY=sk-proj-...

# ElevenLabs
ELEVENLABS_API_KEY=...

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

**Wie nutzt du sie?**
- Lokal: `cp .env.example .env` → in `.env` die echten Werte eintragen → `.env` ist gitignored
- Auf Railway: Variablen direkt im Railway-Dashboard unter "Variables" eintragen

### `CITATION.cff`

Maschinenlesbare Zitierungsinformationen für wissenschaftliche Arbeiten. GitHub zeigt darauf basierend automatisch einen "Cite this repository" Button an.

```yaml
cff-version: 1.2.0
title: Oblivio
authors:
  - family-names: Riccardo
    given-names: Dennis
```

**Angepasst:** Neu für Oblivio.

### `LICENSE`

Lizenzdatei. Bei Oblivio: Academic License (eigene/restriktive Lizenz).

### `mvnw` und `mvnw.cmd`

Maven Wrapper. Erlaubt es, das Projekt zu bauen, **ohne dass Maven lokal installiert sein muss**. Lädt sich Maven selbst herunter beim ersten Lauf.

```bash
./mvnw clean package  # baut das JAR
./mvnw spring-boot:run  # startet lokal
```

**Warum wichtig:** Railway nutzt diesen Wrapper im Dockerfile. Es muss nichts auf dem Railway-Container installiert sein — der Wrapper macht alles selbst.

### `.mvn/wrapper/`

Konfiguration für den Maven Wrapper. Enthält:
- `maven-wrapper.properties` — welche Maven-Version verwendet wird
- `maven-wrapper.jar` — der eigentliche Wrapper-Code

**Diese Dateien NICHT manuell anfassen** — sie kommen von Maven selbst.

---

## 5. Der `src/`-Ordner — Java-Backend

Hier liegt der gesamte Backend-Code. Aufgeteilt in:

```
src/
├── main/
│   ├── java/ch/zhaw/statefulconversation/   ← Quellcode
│   └── resources/                            ← Konfiguration + statische Dateien
└── test/                                     ← Tests (in Oblivio nicht genutzt)
```

### 5.1 `src/main/java/ch/zhaw/statefulconversation/`

Hier liegt der eigentliche Code, aufgeteilt in 7 Pakete:

#### `config/` — Spring-Konfiguration

**`WebConfig.java`** (15 Zeilen) — **NEU IN OBLIVIO**

Erlaubt CORS (Cross-Origin Resource Sharing), damit das Frontend auf Hostpoint mit dem Backend auf Railway sprechen kann.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // alle Domains erlaubt
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
```

**Warum nötig:** Ohne diese Datei würde der Browser jeden API-Aufruf von oblivio.ch zu Railway blockieren.

---

#### `controllers/` — REST-API-Endpoints

Das sind die Klassen, die HTTP-Anfragen entgegennehmen. Jede Klasse mit `@RestController` wird zu einem Endpoint.

**`AgentController.java`** — vom PROMISE-Original übernommen

Behandelt alle Runtime-Endpoints für einen bestehenden Agent:
- `GET /{agentId}/conversation` → Liste aller Nachrichten
- `GET /{agentId}/state` → Aktueller Zustand
- `POST /{agentId}/start` → Gespräch starten
- `POST /{agentId}/respond` → Nachricht senden
- `DELETE /{agentId}/reset` → Agent zurücksetzen

**Was passiert intern:**
```java
@PostMapping("{agentID}/respond")
public ResponseEntity<ResponseView> respond(@PathVariable UUID agentID, @RequestBody UtteranceRequest userSays) {
    Agent agent = repository.findById(agentID).get();  // ← lädt aus Supabase
    Response response = agent.respond(userSays.getContent());  // ← PROMISE-Logik
    this.repository.save(agent);  // ← speichert wieder in Supabase
    return new ResponseEntity<>(new ResponseView(response, agent.isActive()), HttpStatus.OK);
}
```

Hier sieht man perfekt, wie Backend ↔ Supabase fliesst: bei jeder Nachricht wird der Agent aus der DB geladen, verarbeitet und wieder gespeichert.

---

**`AgentMetaController.java`** — Erzeugungs-Endpoints

Erstellt neue Agents. Zwei Endpoints:
- `POST /agent/singlestate` → Legacy-Agent (für Persona-Chat)
- `POST /agent/biographer` → Biographer-Agent

**ANGEPASST IN OBLIVIO:** Der `/agent/biographer`-Endpoint wurde hinzugefügt (im PROMISE-Original gab es nur `/agent/singlestate`).

---

**`AgentMetaUtility.java`** — Factory-Klasse (~580 Zeilen)

Die wichtigste Datei in Oblivio. Hier wird die ganze Biographer-Logik gebaut:

**Methoden:**
- `createSingleStateAgent(data)` → erzeugt einen einfachen Single-State-Agent (PROMISE-Original)
- `createBiographerAgent(data)` → erzeugt den 21-State-Biographer **(NEU IN OBLIVIO)**
- `buildBlockPrompts(language, nickname)` → liefert alle 70 Prompts in der gewünschten Sprache **(NEU)**
- `buildBlockNames()` → liefert die 10 Block-Namen **(NEU)**
- `getLanguageInstruction(language)` → Sprach-Präfix für 8 Sprachen **(NEU)**
- `getFinalPrompt(language)` → Abschluss-Prompt **(NEU)**
- `getFinalStarterPrompt(language, nickname)` → Verabschiedungstext **(NEU)**

**Was sie macht:**
Die `createBiographerAgent()`-Methode baut eine Kette von 21 States rückwärts auf (Final → Block 10 Confirm → Block 10 Conv → ... → Block 1 Conv). Jeder State bekommt seinen System-Prompt aus dem 2D-Array `prompts[10][7]`.

---

**`AgentMetaType.java`** — Enum für Agent-Typen

```java
public enum AgentMetaType {
    singleState(0),
    biographer(1);  // ← NEU IN OBLIVIO
}
```

**ANGEPASST:** Der Wert `biographer = 1` wurde hinzugefügt.

---

**`TTSController.java`** — Text-to-Speech (~100 Zeilen) — **KOMPLETT NEU IN OBLIVIO**

Bridge zwischen Frontend und ElevenLabs. Nimmt Text entgegen, leitet ihn an ElevenLabs weiter und gibt MP3-Bytes zurück.

```java
@PostMapping("{agentID}/tts")
public ResponseEntity<byte[]> textToSpeech(
    @PathVariable String agentID,
    @RequestBody TTSRequest request,
    @RequestParam(value = "voice_id") String voiceId
) {
    String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId;
    // ... ruft ElevenLabs auf
    return ResponseEntity.ok(audioBytes);
}
```

**Warum nicht direkt vom Frontend?** Damit der ElevenLabs-API-Key nicht im Browser sichtbar ist.

---

**`UserLogController.java`** — Multi-User-Endpoints — **KOMPLETT NEU IN OBLIVIO**

```java
@GetMapping("/user/{userId}/agents")       // Liste der Agents
@GetMapping("/user/{userId}/conversations") // Alle Gespräche
@GetMapping("/user/{userId}/stats")         // Statistik
```

**Warum nötig:** PROMISE war Single-User. Oblivio braucht Multi-User-Trennung, also wurden diese Endpoints hinzugefügt.

---

**`controllers/dto/`** — Request-Body-Klassen

DTOs (Data Transfer Objects) sind Klassen, die JSON-Bodies repräsentieren.

- `SingleStateAgentCreateDTO.java` — PROMISE-Original
- `BiographerAgentCreateDTO.java` — **NEU IN OBLIVIO**, enthält zusätzlich `language` und `nickname`

---

**`controllers/views/`** — Response-Klassen

Was an den Client zurückgegeben wird.

- `AgentInfoView.java` — Agent-Info-Response
- `AgentStateInfoView.java` — State-Info
- `ResponseView.java` — Wrapper für Agent-Antworten
- `StorageEntryView.java` — Storage-Einträge
- `TTSRequest.java` — **NEU** (Body für TTS)
- `UserAgentView.java` — **NEU** (für Multi-User-Endpoints)
- `UserConversationView.java` — **NEU**

---

#### `logging/` — Live-Log-Streaming — **KOMPLETT NEUES PAKET IN OBLIVIO**

Vier Klassen, die zusammen einen Live-Log-Stream zum Browser ermöglichen:

**`LogEvent.java`** — Data-Class für ein Log-Event
**`SseLogAppender.java`** — Custom Logback-Appender, fängt alle Logs ab
**`LogStreamBroadcaster.java`** — verteilt Logs an alle Subscriber
**`LogStreamController.java`** — REST-Endpoint `GET /logs/stream` (Server-Sent Events)

**Was es macht:** Wenn du im Browser auf `/logs/stream` zugreifst, siehst du alle Backend-Logs in Echtzeit. Sehr nützlich beim Debugging.

---

#### `model/` — Domain-Modell (State-Machine-Kern)

Diese Klassen sind das Herz von PROMISE. Sie definieren, **was ein Agent ist**.

**`Agent.java`** — Top-Level-Container

```java
@Entity
public class Agent {
    @Id @GeneratedValue
    private UUID id;
    private String name;
    private String userId;  // ← NEU IN OBLIVIO (Multi-User)
    
    @OneToOne private State initialState;
    @OneToOne private State currentState;
    @ManyToOne private Storage storage;
    
    public Response respond(String userSays) {
        try {
            return this.currentState.respond(userSays);
        } catch (TransitionException e) {
            this.currentState = e.getSubsequentState();
            return this.respond(userSays);  // rekursiv im neuen State
        }
    }
}
```

**ANGEPASST IN OBLIVIO:** Die Spalte `userId` wurde hinzugefügt.

---

**`State.java`** — Gesprächszustand

Der wichtigste State der State-Machine. Hat:
- System-Prompt
- Starter-Prompt
- Utterances (Gesprächsverlauf)
- Transitions (mögliche Übergänge)

**Wichtige Methoden:**
```java
public Response respond(String userSays) throws TransitionException {
    this.acknowledge(userSays);              // User-Nachricht hinzufügen
    this.utterances.compactIfNeeded();       // ← NEU IN OBLIVIO (Context Compaction)
    String totalPrompt = this.composeTotalPrompt();
    String assistantSays = LMOpenAI.complete(this.utterances, totalPrompt);
    this.utterances.appendAssistantSays(assistantSays, this);
    return new Response(this, assistantSays);
}
```

**ANGEPASST IN OBLIVIO:**
- Aufruf von `compactIfNeeded()` hinzugefügt
- DB-Spalten `starterPrompt` und `summarisePrompt` auf TEXT geändert (vorher VARCHAR)

---

**`Prompt.java`** — Basisklasse

Alle Prompts (States, Decisions, Actions) erben davon. Wird in einer einzigen DB-Tabelle gespeichert mit `dtype`-Discriminator.

**ANGEPASST IN OBLIVIO:** Spalte `prompt` auf TEXT geändert (vorher VARCHAR(10000)).

---

**`Utterances.java`** — Gesprächsverlauf

Verwaltet die Liste aller Nachrichten in einem State. **NEUE METHODE IN OBLIVIO:**

```java
public void compactIfNeeded() {
    long userMessageCount = utteranceList.stream()
        .filter(u -> "user".equals(u.getRole())).count();
    
    if (userMessageCount <= 20) return;
    
    // ... ältere Nachrichten zusammenfassen via LMOpenAI.summariseOffline()
    // ... durch eine einzige System-Message ersetzen
}
```

**Warum nötig:** Bei langen Gesprächen würden die Token-Kosten explodieren. Diese Methode komprimiert ältere Nachrichten zu einer Zusammenfassung.

---

**`Utterance.java`** — Einzelne Nachricht

Hat `role` (user/assistant/system), `content` (Text), `createdDate`, `stateName`.

**ANGEPASST IN OBLIVIO:** Spalte `content` auf TEXT geändert.

---

**`Transition.java`** — Verbindung zwischen States

```java
public boolean decide(Utterances utterances) {
    for (Decision current : this.decisions) {
        boolean result = LMOpenAI.decide(utterances, current.getPrompt());
        if (!result) return false;  // UND-Logik
    }
    return true;
}

public void action(Utterances utterances) {
    for (Action current : this.actions) {
        current.execute(utterances);  // sequentiell
    }
}
```

**Unverändert** von PROMISE-Original.

---

**`Decision.java`**, **`Action.java`** — abstrakte Basisklassen
**`Storage.java`**, **`StorageEntry.java`** — Key-Value-Speicher
**`Response.java`**, **`PromptResult.java`** — Wrapper-Klassen
**`Final.java`** — End-State
**`TransitionException.java`** — Exception für State-Wechsel
**`OuterState.java`** — verschachtelte States (nicht genutzt)

Alle **unverändert** von PROMISE.

---

#### `model/commons/` — Wiederverwendbare Bausteine

Aus PROMISE übernommen, unverändert:

**`actions/`:**
- `StaticExtractionAction.java` — extrahiert Daten ins Storage
- `TransferUtterancesAction.java` — kopiert Utterances zwischen States
- `StaticSummarisationAction.java` — fasst Gespräch zusammen
- `RemoveLastUtteranceAction.java` — entfernt letzte User-Nachricht
- `DynamicExtractionAction.java` — dynamische Variante
- `DynamicExtractionActionPrimitive.java`
- `DynamicRemoveTopicAction.java`

**`decisions/`:**
- `StaticDecision.java` — fester Guard-Prompt
- `DynamicDecision.java` — Guard mit Storage-Daten
- `DynamicDecisionPrimitive.java`

**`states/`:** ~14 spezialisierte State-Typen (alle ungenutzt in Oblivio)

---

#### `repositories/` — JPA-Datenbankzugriff

Interfaces für CRUD-Operationen auf den Tabellen:

```java
public interface AgentRepository extends JpaRepository<Agent, UUID> {}
```

Spring/JPA implementiert diese Interfaces automatisch — du brauchst keine SQL-Queries zu schreiben.

**Alle Dateien unverändert** von PROMISE:
- `AgentRepository.java`
- `StateRepository.java`
- `StorageRepository.java`
- `StorageEntryRepository.java`
- `UtteranceRepository.java`
- `UtterancesRepository.java`

---

#### `spi/` — Service Provider Interface (OpenAI-Anbindung)

**`LMOpenAI.java`** — die zentrale OpenAI-Bridge

Enthält alle Methoden zur Kommunikation mit GPT-4o:
- `complete(...)` → Antwort generieren
- `decide(...)` → true/false-Antwort für Guards
- `extract(...)` → JSON-Daten extrahieren
- `summarise(...)` → JSON-Zusammenfassung
- `summariseOffline(...)` → **NEU IN OBLIVIO** (Plain-Text-Zusammenfassung für Context Compaction)

**`OpenAIProperties.java`** — lädt API-Key aus Properties-Datei. Unverändert.

**`ContenFilterException.java`** — Exception bei Content-Filter. Unverändert.

**`GsonExclude.java`** — Annotation für JSON-Exclude. Unverändert.

---

#### `utils/`

**`NamedParametersFormatter.java`** — Hilfsklasse zum Ersetzen von Platzhaltern. Unverändert von PROMISE.

---

#### `StatefulconversationApplication.java`

Der Spring Boot Entry Point:

```java
@SpringBootApplication
public class StatefulconversationApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatefulconversationApplication.class, args);
    }
}
```

**Unverändert** von PROMISE.

---

### 5.2 `src/main/resources/` — Konfiguration und Ressourcen

#### `application.properties.template`

Vorlage für lokale Entwicklung. Du kopierst sie zu `application.properties` (gitignored) und trägst deine Werte ein:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/oblivio
spring.datasource.username=postgres
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update
```

#### `application-prod.properties` — **NEU IN OBLIVIO**

Wird **nur in Produktion** verwendet (Railway setzt `SPRING_PROFILES_ACTIVE=prod`):

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.data-source-properties.prepareThreshold=0  # für PgBouncer
```

Die `${...}` werden von Railway aus Env-Variablen befüllt.

#### `openai.properties.template`

Vorlage für OpenAI-Konfig:
```properties
openai.url=https://api.openai.com/v1/chat/completions
openai.key=sk-...
openai.model=gpt-4o
```

#### `openai-prod.properties` — **NEU IN OBLIVIO**

Produktions-Variante mit Env-Variable:
```properties
openai.key=${OPENAI_KEY}
```

#### `logback-spring.xml`

Logging-Konfiguration. **ANGEPASST IN OBLIVIO** um den SSE-Appender hinzuzufügen:

```xml
<appender name="SSE" class="ch.zhaw.statefulconversation.logging.SseLogAppender" />
<root level="INFO">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="SSE" />  <!-- ← neu -->
</root>
```

#### `public/` — Statische PROMISE-Demo-Seiten

Diese Ordner enthält von Spring Boot direkt ausgelieferte statische HTML-Dateien.

**Wichtig:** Diese werden NICHT für Oblivio.ch genutzt — sie sind das alte PROMISE-Demo-Frontend.

- `index.html`, `script.js`, `style.css` — einfache Demo-Seite
- `monitor/` — Live-Log-Monitor (mit SSE)
- `realtime/` — Realtime-API-Demo (experimentell)

**Lassen wir drin**, weil sie nützlich beim Debuggen sind.

---

### 5.3 `src/test/` — Tests

In Oblivio nicht aktiv genutzt. Enthält noch PROMISE-Testbeispiele:
- `bots/SingleStateInteraction.java`, `MultiStateInteraction.java`, ...
- `model/NamedParametersFormatterTest.java`, ...
- `persistence/ExtractionActionPersistenceTest.java`, ...

**Diese werden im Dockerfile mit `-DskipTests` übersprungen.**

---

## 6. Der `sql/`-Ordner — Datenbank-Skripte

Zwei SQL-Dateien, die die Oblivio-spezifischen Tabellen anlegen:

### `sql/SUPABASE_TABLES.sql` — Initiale Tabellenstruktur

Wird **einmalig** beim Setup ausgeführt. Enthält:

```sql
CREATE TABLE user_agents ( ... );
CREATE TABLE user_legacies ( ... );
CREATE TABLE legacy_access_codes ( ... );
CREATE TABLE legacy_messages ( ... );
CREATE TABLE questionnaire_answers ( ... );

-- RLS-Policies
ALTER TABLE user_agents ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can read own agents" ON user_agents
    FOR SELECT USING (auth.uid() = user_id);
```

**Wie ausführen:**
- Variante 1: Im Supabase SQL Editor copy-paste
- Variante 2: `psql <connection-string> -f sql/SUPABASE_TABLES.sql`

### `sql/supabase_migrations.sql` — Spätere Erweiterungen

Migrations-Skript für nachträgliche Änderungen. Enthält z.B. Spalten, die nach dem initialen Setup hinzugefügt wurden:

```sql
ALTER TABLE legacy_messages ADD COLUMN IF NOT EXISTS visitor_name TEXT;
ALTER TABLE legacy_messages ADD COLUMN IF NOT EXISTS user_id UUID DEFAULT auth.uid();
ALTER TABLE legacy_access_codes ADD COLUMN IF NOT EXISTS voice_id TEXT;
ALTER TABLE legacy_access_codes ADD COLUMN IF NOT EXISTS avatar_url TEXT;
```

Bei einem Nachbau einfach **nach** `SUPABASE_TABLES.sql` ausführen.

---

## 7. Der `docs/`-Ordner — Dokumentation

Hier liegen alle Markdown-Dokumente und ein Jupyter-Notebook:

- **`OBLIVIO_BUILD_GUIDE.md`** — Vollständige technische Doku (20 Kapitel)
- **`OBLIVIO_GITHUB_GUIDE.md`** — Dieses Dokument
- **`QUICKSTART.md`** — Schnellstart-Anleitung
- **`PROMISE_INTEGRATION_GUIDE.md`** — Wie Oblivio PROMISE integriert
- **`FRONTEND_INTEGRATION_STEPS.md`** — Schritt-für-Schritt Frontend-Setup
- **`INTEGRATION_COMPLETE.md`** — Übersicht aller integrierten Komponenten
- **`ELEVENLABS_SETUP.md`** — TTS-Setup
- **`RAILWAY_SUPABASE_DEPLOYMENT.md`** — Deployment-Anleitung
- **`USER_TRACKING_FEATURES.md`** — Multi-User-Tracking
- **`REALTIMEDME.md`** — Realtime-API-Doku (experimentell)
- **`PROMISE_Realtime.ipynb`** — Jupyter-Notebook für Realtime-Tests

**Diese Dateien werden bei Bedarf von Railway ignoriert** (siehe `.railwayignore`).

---

## 8. Was NICHT auf GitHub liegt

Wichtige Komponenten von Oblivio liegen bewusst **ausserhalb** des Repos:

| Was | Wo | Warum nicht im Repo |
|---|---|---|
| **Frontend (`Website/`)** | Hostpoint via FTP | Statische Seiten, eigener Hosting-Workflow |
| **`application.properties`** | Lokal + Railway Env-Vars | Enthält Datenbank-Passwort |
| **`openai.properties`** | Lokal + Railway Env-Vars | Enthält API-Key |
| **`Personas/`** | Lokal | Persönliche Daten von Teilnehmenden |
| **Bachelorarbeit (`.docx`)** | Lokal | Persönliches Dokument |
| **`PROMISE_TECHNICAL_DOCUMENTATION.md`** | Lokal | Persönliche Notizen |

**Warum die strikte Trennung?**
- **Sicherheit:** API-Keys und Passwörter dürfen NIE in Git landen
- **Datenschutz:** Teilnehmer-Daten gehören nicht in ein öffentliches Repo
- **Trennung der Belange:** Frontend hat einen anderen Deployment-Workflow

---

## 9. Workflow: Vom Code-Change zum Live-Deployment

So sieht der typische Entwicklungsprozess aus:

```
1. Lokal: Code-Änderung in src/main/java/...
        │
        ▼
2. Lokal testen: ./mvnw spring-boot:run
        │
        ▼
3. git add . && git commit -m "Beschreibung"
        │
        ▼
4. git push origin main
        │
        ▼
5. GitHub registriert Push, sendet Webhook an Railway
        │
        ▼
6. Railway startet Docker-Build (2-5 Min)
        │
        ▼
7. Build erfolgreich → neuer Container ersetzt alten
        │
        ▼
8. Neue Version live auf https://promise-production.up.railway.app
```

**Bei Frontend-Änderungen:**

```
1. Lokal: Änderung in Website/legacy.html
        │
        ▼
2. FTP-Upload zu Hostpoint (manuell)
        │
        ▼
3. Sofort live auf oblivio.ch
```

**Bei DB-Änderungen:**

```
1. Lokal: Neue Spalte im Java-Code (z.B. Agent.userId)
        │
        ▼
2. Push zu GitHub → Railway deployed
        │
        ▼
3. Hibernate erkennt neue Spalte beim Start → ALTER TABLE in Supabase
        │
        ▼
4. ABER: Bei Typänderungen (VARCHAR → TEXT) muss man MANUELL in Supabase
   den ALTER TABLE ... TYPE TEXT ausführen!
```

---

## 10. Anpassungen pro Datei — was und warum

Hier nochmal die wichtigsten Anpassungen kompakt:

### Backend-Anpassungen

| Datei | Was wurde angepasst | Warum |
|---|---|---|
| `pom.xml` | PostgreSQL-Driver hinzugefügt, MySQL entfernt | Wir nutzen Supabase (PostgreSQL) |
| `Dockerfile` | Komplett neu, Multi-Stage Build | Für Railway-Deployment |
| `railway.json` | Komplett neu | Railway-Konfig |
| `.gitignore` | Erweitert um Oblivio-spezifische Einträge | Persönliche Dateien ausschliessen |
| `WebConfig.java` | Komplett neu | CORS für Frontend-Backend-Kommunikation |
| `Agent.java` | Spalte `userId` hinzugefügt | Multi-User-Tracking |
| `State.java` | `compactIfNeeded()` Aufruf + TEXT-Spalten | Context Compaction + lange Prompts |
| `Prompt.java` | TEXT statt VARCHAR(10000) | Lange Persona-Prompts |
| `Utterance.java` | TEXT statt VARCHAR(4096) | Lange Antworten |
| `Utterances.java` | `compactIfNeeded()`-Methode (60 Zeilen neu) | Token-Optimierung |
| `LMOpenAI.java` | `summariseOffline()`-Methode hinzugefügt | Für Context Compaction |
| `AgentMetaController.java` | `/agent/biographer`-Endpoint hinzugefügt | Biographer-Erzeugung |
| `AgentMetaUtility.java` | `createBiographerAgent()` + 70 Block-Prompts | Biographer-Logik |
| `AgentMetaType.java` | Enum-Wert `biographer = 1` hinzugefügt | Neuer Agent-Typ |
| `TTSController.java` | Komplett neu (100 Zeilen) | ElevenLabs-Integration |
| `UserLogController.java` | Komplett neu | Multi-User-Endpoints |
| `BiographerAgentCreateDTO.java` | Neu | Request-Body für Biographer |
| `TTSRequest.java` | Neu | Request-Body für TTS |
| `UserAgentView.java`, `UserConversationView.java` | Neu | Multi-User-Responses |
| `logging/` (4 Dateien) | Komplett neues Paket | Live-Log-Streaming |
| `application-prod.properties` | Komplett neu | Produktions-Config |
| `openai-prod.properties` | Komplett neu | Produktions-OpenAI-Config |
| `logback-spring.xml` | SSE-Appender hinzugefügt | Live-Logs |

### Was NICHT angepasst wurde (PROMISE-Original)

- Alle anderen `model/`-Klassen (Decision, Action, Storage, Transition, ...)
- `model/commons/` (alle Actions, Decisions, States)
- `repositories/` (alle JPA-Repos)
- `spi/OpenAIProperties.java`, `ContenFilterException.java`, `GsonExclude.java`
- `controllers/AgentController.java`
- `utils/NamedParametersFormatter.java`
- `StatefulconversationApplication.java`

---

## Anhang: GitHub-Commit-History-Lesen

Wenn du im Repo `git log` ausführst, siehst du die wichtigsten Meilensteine:

```bash
git log --oneline

# Beispiel-Ausgabe:
# 3df7948 Add comments to all config, resource, SQL, and test files
# c148792 Add Javadoc and inline comments to all Java source files
# 92a90de Remove persona table and ElevenLabs reference from README
# b36c759 Restructure repo: move docs and SQL into folders, restrict CORS, clean up
# 0cd5316 Add automatic context compaction after 20 user messages
# ...
```

Jeder Commit ist eine logische Einheit. Wenn du verstehen willst, wann was hinzugefügt wurde:

```bash
git log --all -- src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java
# Zeigt alle Änderungen an dieser Datei
```

---

**Autor:** Dennis Riccardo
**Institution:** ZHAW School of Management and Law, Business Informatics
**Bachelorarbeit:** 2026
**Repository:** https://github.com/riccaden/promise
**Lizenz:** Academic
