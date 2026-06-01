# Dateien-Übersicht — Oblivio

Kompakte deutsche Zusammenfassungen aller für Oblivio relevanten Dateien und ihrer wichtigsten Funktionen. Pro Datei ist vermerkt ob sie **neu** (NEU), **aus PROMISE übernommen und angepasst** (ANGEPASST) oder **unverändert von PROMISE** (UNVERÄNDERT) ist.

Das Dokument ergänzt das [Haupt-README](../README.md) und den [Build-Guide](OBLIVIO_BUILD_GUIDE.md): es dient als Nachschlagewerk wenn man eine konkrete Datei oder Funktion verstehen möchte, ohne den ganzen Quellcode lesen zu müssen.

---

## Inhaltsverzeichnis

1. [Backend — Bootstrap & Konfiguration](#1-backend--bootstrap--konfiguration)
2. [Backend — Controllers](#2-backend--controllers)
3. [Backend — Logging-Paket](#3-backend--logging-paket)
4. [Backend — Domain-Modell](#4-backend--domain-modell)
5. [Backend — PROMISE Commons](#5-backend--promise-commons)
6. [Backend — Repositories](#6-backend--repositories)
7. [Backend — SPI (OpenAI-Schicht)](#7-backend--spi-openai-schicht)
8. [Backend — Properties & Ressourcen](#8-backend--properties--ressourcen)
9. [Frontend — HTML-Seiten](#9-frontend--html-seiten)
10. [Frontend — JavaScript-Module](#10-frontend--javascript-module)
11. [Datenbank — SQL-Skripte](#11-datenbank--sql-skripte)
12. [Infrastruktur — Docker, Railway, Maven](#12-infrastruktur--docker-railway-maven)

---

## 1. Backend — Bootstrap & Konfiguration

### `StatefulconversationApplication.java` (UNVERÄNDERT)

Der Einstiegspunkt der Spring-Boot-Anwendung. Enthält die `main()`-Methode und die `@SpringBootApplication`-Annotation, die das Component-Scanning, Auto-Configuration und Embedded-Tomcat aktiviert.

| Funktion | Aufgabe |
|---|---|
| `main(String[] args)` | Startet den Spring-Container via `SpringApplication.run()`. Beim Start lädt Spring die `application.properties` (oder `application-prod.properties` falls Profil `prod` aktiv), öffnet die Datenbankverbindung, lässt Hibernate die Tabellen aktualisieren und bindet den HTTP-Listener auf Port 8080. |

### `config/WebConfig.java` (NEU)

CORS-Konfiguration. Erlaubt dem Frontend auf Hostpoint (oblivio.ch), Anfragen ans Backend auf Railway zu stellen, ohne dass der Browser die Same-Origin-Policy abweist.

| Funktion | Aufgabe |
|---|---|
| `addCorsMappings(CorsRegistry registry)` | Registriert `/**` (alle Endpunkte) für Cross-Origin-Requests. Erlaubt die HTTP-Methoden GET/POST/PUT/DELETE/OPTIONS und alle Header. In Produktion sollte `allowedOriginPatterns("*")` auf die konkrete Domain eingeschränkt werden. |

---

## 2. Backend — Controllers

### `controllers/AgentController.java` (UNVERÄNDERT)

Der Runtime-Controller für bestehende Agents — wird bei jeder Nachricht in einer laufenden Konversation aufgerufen. Stock-PROMISE.

| Endpunkt | Funktion | Aufgabe |
|---|---|---|
| `GET /{agentID}/conversation` | `getConversation()` | Gibt alle Nachrichten (Utterances) des Agents als Liste zurück. |
| `GET /{agentID}/state` | `getState()` | Gibt den Namen des aktuellen Zustands und ob der Agent noch aktiv ist (`isActive()`) zurück. |
| `POST /{agentID}/start` | `start()` | Triggert die `start()`-Methode auf dem aktuellen State — generiert die Eröffnungsnachricht. |
| `POST /{agentID}/respond` | `respond()` | Hauptmethode: lädt den Agent aus Supabase, ruft `agent.respond(userSays)` auf, persistiert den modifizierten Agent, gibt die Antwort zurück. |
| `DELETE /{agentID}/reset` | `reset()` | Setzt `currentState` zurück auf `initialState`, leert alle Utterances. |
| `GET /{agentID}/storage` | `getStorage()` | Gibt alle StorageEntries (z.B. die 10 Block-Zusammenfassungen am Ende des Biographen) zurück. |

### `controllers/AgentMetaController.java` (ANGEPASST)

Factory-Endpunkte zum Erzeugen neuer Agents. Im PROMISE-Original gab es nur `/agent/singlestate`; Oblivio hat `/agent/biographer` hinzugefügt.

| Endpunkt | Funktion | Aufgabe |
|---|---|---|
| `POST /agent/singlestate` | `createSingleStateAgent()` | Erzeugt einen einfachen Single-State-Agent mit einem Prompt und einer Übergangsbedingung zu Final. Wird in Oblivio für die Persona-Chats verwendet. |
| `POST /agent/biographer` (**NEU**) | `createBiographerAgent()` | Erzeugt den 21-State-Biographer (10 Blöcke × 2 + Final). Delegiert die Konstruktion an `AgentMetaUtility.createBiographerAgent()`. |

### `controllers/AgentMetaType.java` (ANGEPASST)

Enum für die Agent-Typen, wird vom Controller zur DTO-Validierung benutzt.

| Wert | Bedeutung |
|---|---|
| `singleState = 0` | Persona-Agent (PROMISE-Original) |
| `biographer = 1` (**NEU**) | Biographer-Agent (Oblivio) |

### `controllers/AgentMetaUtility.java` (ANGEPASST, ~549 Zeilen)

**Die zentrale Datei für Oblivio.** Enthält die ganze Biographer-Logik und die 70 deutschsprachigen Block-Prompts. Die Methoden `createSingleStateAgent()` und `createBiographerAgent()` werden vom Controller aufgerufen.

| Funktion | Aufgabe |
|---|---|
| `createSingleStateAgent(SingleStateAgentCreateDTO data)` | **UNVERÄNDERT von PROMISE** — baut einen Agent mit einem State und einer Transition zu Final. Setzt `agent.userId` aus dem DTO (Oblivio-Erweiterung). |
| `createBiographerAgent(BiographerAgentCreateDTO data)` (**NEU**) | Baut den 21-State-Biographer rückwärts auf: zuerst Final, dann Block 10 Confirm, Block 10 Conv, Block 9 Confirm, … bis Block 1 Conv. Wickelt jeden State mit `TransferUtterancesAction` (Conv→Confirm) und `StaticExtractionAction` (Confirm→nächster Block) ein. Erstellt einen gemeinsamen `Storage`-Container. |
| `buildBlockPrompts(String language, String nickname)` (**NEU**) | Liefert ein `String[10][7]`-Array mit allen 70 Block-Prompts. Die Prompts sind statisch auf Deutsch hinterlegt; bei nicht-deutscher Sprache wird `getLanguageInstruction()` als Präfix gesetzt. Die 7 Komponenten pro Block sind: System, Starter, Summarise, Guard-Conv→Confirm, Guard-Confirm→Next, Extract, Final-Hint. |
| `buildBlockNames()` (**NEU**) | Gibt die 10 Block-Titel zurück (z.B. "Block 1 - Identität & Herkunft"). |
| `getLanguageInstruction(String language)` (**NEU**) | Gibt einen Sprach-Präfix zurück, der den 70 deutschen Prompts vorangestellt wird: *"Folgende Anweisungen sind auf Deutsch, du MUSST aber ausschliesslich auf Koreanisch antworten."* Spart 8×70=560 String-Übersetzungen — stattdessen 70 + 8. |
| `getFinalPrompt(String language)` (**NEU**) | Gibt den Prompt für den Final-State zurück: ein Verabschiedungstext nach erfolgreichem Abschluss aller 10 Blöcke. |
| `getFinalStarterPrompt(String language, String nickname)` (**NEU**) | Gibt den Starter-Prompt für den Final-State zurück (der erste Satz, den die KI sagt sobald sie diesen State erreicht). |

### `controllers/TTSController.java` (NEU, ~117 Zeilen)

Brücke zwischen Frontend und ElevenLabs. Nimmt Text vom Frontend entgegen, ruft die ElevenLabs-API auf und gibt MP3-Bytes zurück. Hält den ElevenLabs-API-Key serverseitig.

| Endpunkt | Funktion | Aufgabe |
|---|---|---|
| `TTSController()` | Konstruktor | Lädt `ELEVENLABS_API_KEY` aus Environment-Variablen. |
| `POST /{agentID}/tts?voice_id=...` | `textToSpeech()` | Schickt den übergebenen Text an `https://api.elevenlabs.io/v1/text-to-speech/{voiceId}` mit dem Modell `eleven_multilingual_v2`. Gibt die MP3-Bytes als HTTP-Body zurück. Bei fehlendem API-Key oder Voice-ID gibt es 4xx zurück. |

### `controllers/UserLogController.java` (NEU, ~199 Zeilen)

Multi-User-Endpunkte für die Journey-Dashboard-Seite. Filtert alle Datenbankabfragen über `agent.userId`.

| Endpunkt | Funktion | Aufgabe |
|---|---|---|
| `GET /user/{userId}/agents` | `getUserAgents()` | Liefert alle Agents eines Users (ID, Name, Beschreibung, ob aktiv, Anzahl Nachrichten) als `List<UserAgentView>`. |
| `GET /user/{userId}/conversations` | `getUserConversations()` | Sammelt sämtliche Nachrichten aller Agents eines Users in einer flachen Liste. Optional gefiltert mit `?after=<ISO-Timestamp>` für Inkremental-Loads. |
| `GET /user/{userId}/agent/{agentId}/conversation` | `getUserAgentConversation()` | Gibt nur die Nachrichten eines bestimmten Agents zurück — schneller als das vollständige `/conversations`, wenn man nur einen Agent anzeigen möchte. |
| `GET /user/{userId}/stats` | `getUserStats()` | Liefert `UserStatsView`: Gesamtanzahl Agents, Anzahl aktiver Agents, Gesamtanzahl Nachrichten. Für die Dashboard-Kennzahlen. |

### DTOs — `controllers/dto/`

**`SingleStateAgentCreateDTO.java`** (UNVERÄNDERT) — Request-Body für `/agent/singlestate`. Felder: `type`, `agentName`, `description`, `prompt`, `starterPrompt`, `summarisePrompt`, optional `userId`.

**`BiographerAgentCreateDTO.java`** (NEU) — Request-Body für `/agent/biographer`. Erweitert `SingleStateAgentCreateDTO` um zwei Felder: `language` (z.B. `"de"`) und `nickname` (z.B. `"Maria"`). Werden in den Block-Prompts verwendet.

### Views — `controllers/views/`

Diese Klassen sind reine Datentransfer-Objekte, die als JSON-Response zurückgeschickt werden:

| Datei | Status | Inhalt |
|---|---|---|
| `AgentInfoView.java` | UNVERÄNDERT | UUID + Name + Aktiv-Status eines Agents (Response nach `POST /agent/...`). |
| `AgentStateInfoView.java` | UNVERÄNDERT | Aktueller State-Name + Aktiv-Status. |
| `ResponseView.java` | UNVERÄNDERT | Antwort + neuer State-Name + Aktiv-Status nach `respond()`. |
| `StorageEntryView.java` | UNVERÄNDERT | Key + Wert eines Storage-Eintrags. |
| `UtteranceRequest.java` | UNVERÄNDERT | Request-Body mit `content`-Feld für `POST /respond`. |
| `TTSRequest.java` | **NEU** | Request-Body mit `text`-Feld für `POST /tts`. |
| `UserAgentView.java` | **NEU** | Agent-Info mit zusätzlichen Statistiken (Nachrichtenanzahl). |
| `UserConversationView.java` | **NEU** | Eine einzelne Nachricht mit Agent-Kontext (Agent-ID, Agent-Name, Role, Content, Timestamp). |

---

## 3. Backend — Logging-Paket

Komplett neues Paket in Oblivio (~4 Dateien). Stellt Live-Log-Streaming über Server-Sent Events bereit, damit Logs während des Betriebs direkt im Browser sichtbar sind ohne Railway-CLI.

### `logging/LogEvent.java` (NEU)

Schlanke Data-Class für ein einzelnes Log-Event. Felder: `timestamp` (Instant), `level` (INFO/WARN/ERROR), `logger` (Klassenname), `message` (Text).

### `logging/SseLogAppender.java` (NEU, ~28 Zeilen)

Custom Logback-Appender. Bei jedem Log-Event im Spring-Boot-Backend ruft Logback automatisch `append(ILoggingEvent event)` auf — dort wird das Event in ein `LogEvent` gewrappt und an den `LogStreamBroadcaster` weitergeleitet. Wird in `logback-spring.xml` als zusätzlicher Appender registriert.

### `logging/LogStreamBroadcaster.java` (NEU, ~56 Zeilen)

Singleton-Komponente. Verwaltet eine Liste aktiver SSE-Subscriber.

| Funktion | Aufgabe |
|---|---|
| `subscribe(SseEmitter emitter)` | Fügt einen neuen Subscriber hinzu — wird vom Controller aufgerufen wenn ein Browser `/logs/stream` öffnet. |
| `broadcast(LogEvent event)` | Schickt das Event an alle aktiven Subscriber. Gescheiterte Sends (z.B. geschlossene Verbindungen) werden aus der Liste entfernt. |

### `logging/LogStreamController.java` (NEU)

REST-Controller mit nur einem Endpunkt.

| Endpunkt | Funktion | Aufgabe |
|---|---|---|
| `GET /logs/stream` | `streamLogs()` | Öffnet einen `SseEmitter` für den Client, registriert ihn beim Broadcaster und hält die HTTP-Verbindung offen. Der Browser empfängt von da an alle Backend-Logs in Echtzeit als Server-Sent Events. |

---

## 4. Backend — Domain-Modell

Das Herz von PROMISE. Diese Klassen definieren *was ein Agent ist*. Drei davon hat Oblivio angepasst, der Rest ist unverändert.

### `model/Prompt.java` (ANGEPASST)

Basisklasse aller Prompts (System-, Starter-, Guard-, Action-Prompts). Verwendet JPA `SINGLE_TABLE`-Inheritance — alle Subklassen liegen in einer Tabelle `prompt` mit `dtype`-Discriminator.

| Anpassung | Was wurde geändert |
|---|---|
| `@Column(length = 10000)` → `@Column(columnDefinition = "TEXT")` | Persona-Prompts werden bis 22.000 Zeichen lang; das alte VARCHAR-Limit hätte Inserts gecrasht. |

### `model/State.java` (ANGEPASST)

Der wichtigste Zustand der State-Machine. Hat einen System-Prompt, einen Starter-Prompt, einen Summarise-Prompt, eine `Utterances`-Historie und mehrere `Transition`-Objekte.

| Funktion | Aufgabe |
|---|---|
| `respond(String userSays)` | Die zentrale Methode pro Nachricht. Reihenfolge: (1) `acknowledge(userSays)` — fügt die User-Nachricht hinzu und prüft Transitions; (2) **`compactIfNeeded()` (NEU in Oblivio)** — komprimiert lange Historien; (3) `composeTotalPrompt()` — baut den finalen Prompt; (4) `LMOpenAI.complete()` — fragt GPT-4o; (5) `appendAssistantSays()` — speichert die Antwort. |
| `start()` | Generiert die Eröffnungsnachricht des States via Starter-Prompt. |
| `summarise()` | Erstellt eine JSON-Zusammenfassung der Konversation (für Block-Summaries). |

Zusätzlich wurden die Spalten `starterPrompt` und `summarisePrompt` auf TEXT geändert (gleicher Grund wie bei Prompt.java).

### `model/Utterance.java` (ANGEPASST)

Eine einzelne Nachricht. Felder: `role` (user/assistant/system), `content`, `createdDate`, `stateName`.

| Anpassung | Was wurde geändert |
|---|---|
| `@Column(length = 4096)` → `@Column(columnDefinition = "TEXT")` für `content` | LLM-Antworten können länger als 4096 Zeichen werden, vor allem bei Persona-Erzählungen. |

### `model/Utterances.java` (ANGEPASST)

Container für die Nachrichten-Historie eines States. Stock-PROMISE bietet `appendUserSays()`, `appendAssistantSays()`, `removeLastUtterance()`, `reset()`. Oblivio hat dazu:

| Funktion | Aufgabe |
|---|---|
| `compactIfNeeded()` (**NEU**, ~60 Zeilen) | Zählt die User-Nachrichten. Falls >20 *und* noch nicht komprimiert (erkannt am Tag `[Zusammenfassung des bisherigen Gesprächs]`): nimmt alle bis auf die letzten 10 Nachrichten, schickt sie an `LMOpenAI.summariseOffline()`, löscht die Originale via JPA-Orphan-Removal, prependet eine einzelne System-Nachricht mit der Zusammenfassung. Resultat: Token-Verbrauch bleibt flach unabhängig von der Gesprächslänge. |

### `model/Agent.java` (ANGEPASST)

Top-Level-Entity. Hält die Referenz auf den `initialState`, `currentState`, `Storage`, und ist die Wurzel jeder Hibernate-Cascade-Operation.

| Funktion | Aufgabe |
|---|---|
| `respond(String userSays)` | Delegiert an `currentState.respond()`. Fängt `TransitionException`, setzt `currentState` neu, ruft sich rekursiv selbst auf — so können mehrere Transitionen in einer HTTP-Anfrage hintereinander feuern. |
| `start()` | Delegiert an `currentState.start()`. |
| `reset()` | Setzt `currentState = initialState` und löscht alle Utterances. |
| `isActive()` | Gibt `false` zurück sobald `currentState instanceof Final`. |
| `getUserId() / setUserId()` (**NEU**) | Getter/Setter für die Oblivio-Erweiterung `userId`. Hibernate fügt die Spalte beim Start automatisch hinzu (ddl-auto=update). |

### `model/Transition.java` (UNVERÄNDERT)

Bindet einen State an seinen Nachfolger. Hat eine Liste von `Decision`-Objekten (UND-verknüpft als Guard) und eine Liste von `Action`-Objekten (sequenziell ausgeführt beim Übergang).

| Funktion | Aufgabe |
|---|---|
| `decide(Utterances utterances)` | Iteriert über alle Decisions; ein einziges `false` reicht zum Abbruch. |
| `action(Utterances utterances)` | Führt jede Action sequenziell aus. |

### `model/Decision.java` (UNVERÄNDERT)

Abstrakte Basisklasse für Guards. Stock-PROMISE bietet `StaticDecision` und `DynamicDecision` als Subklassen.

### `model/Action.java` (UNVERÄNDERT)

Abstrakte Basisklasse für Aktionen. Stock-PROMISE bietet u.a. `TransferUtterancesAction`, `StaticExtractionAction`, `StaticSummarisationAction`.

### `model/Storage.java` & `model/StorageEntry.java` (UNVERÄNDERT)

Key-Value-Store, der pro Agent existiert. Wird vom Biographen genutzt um die 10 Block-Summaries unter den Keys `block1`…`block10` zu speichern. Frontend liest sie via `GET /{agentId}/storage` aus.

### `model/Final.java` (UNVERÄNDERT)

Spezial-State, der die Konversation als beendet markiert. `isActive()` gibt immer `false` zurück. Hat keine ausgehende Transition.

### `model/Response.java` (UNVERÄNDERT)

Wrapper aus State + Antworttext, wird intern zwischen `State.respond()` und `Agent.respond()` durchgereicht.

### `model/PromptResult.java` (UNVERÄNDERT)

Wrapper für LLM-Antworten — enthält den Text plus optional strukturierte Felder (z.B. bei JSON-Extraktion).

### `model/TransitionException.java` (UNVERÄNDERT)

Wird von `Transition.action()` geworfen, wenn der Guard `true` zurückgegeben hat. Trägt den `subsequentState` als Feld — der Catch-Block in `Agent.respond()` liest ihn aus und setzt `currentState` neu. Klassisches "Exceptions as Control Flow"-Pattern.

### `model/OuterState.java` (UNVERÄNDERT, in Oblivio ungenutzt)

Erlaubt verschachtelte State-Machines (ein State, der intern wieder eine eigene Sub-State-Machine enthält). PROMISE bietet das an, Oblivio nutzt es nicht.

---

## 5. Backend — PROMISE Commons

Wiederverwendbare Decision- und Action-Implementierungen aus PROMISE. **Alle UNVERÄNDERT.** Oblivio nutzt drei davon aktiv:

### `model/commons/decisions/StaticDecision.java`

Ein Guard mit festem Prompt-Text. Beispiel: *"Wurden alle 11 Fragen beantwortet? Antworte mit yes oder no."* Schickt den Prompt + die Konversationshistorie an `LMOpenAI.decide()`, das einen Boolean zurückgibt.

### `model/commons/actions/StaticExtractionAction.java`

Beim Auslösen schickt sie die Konversation an `LMOpenAI.extract()` mit einem Extraktionsschema. Das Ergebnis (JSON-Objekt) wird unter einem festen Key in die `Storage` des Agents geschrieben. In Oblivio: schreibt die Block-Summaries `block1`…`block10`.

### `model/commons/actions/TransferUtterancesAction.java`

Kopiert alle Utterances aus dem aktuellen State in den Ziel-State der Transition. In Oblivio: beim Übergang von Conv → Confirm bekommt der Confirm-State so die volle Gesprächshistorie zum Zusammenfassen.

### Übrige Commons (in Oblivio ungenutzt aber im Repo vorhanden)

- `DynamicDecision`, `DynamicDecisionPrimitive` — Guards die ihre Prompt-Texte zur Laufzeit aus dem Storage befüllen
- `DynamicExtractionAction`, `DynamicExtractionActionPrimitive`, `DynamicRemoveTopicAction` — Actions mit Laufzeit-Variablen
- `StaticSummarisationAction` — schreibt eine JSON-Zusammenfassung in Storage
- `RemoveLastUtteranceAction` — entfernt die letzte User-Nachricht
- 14 spezialisierte State-Klassen in `model/commons/states/` (GatherState, SmallTalkState, MentalWellbeingAssessmentState, etc.) — alle aus PROMISE-Demos, in Oblivio nicht verwendet

---

## 6. Backend — Repositories

Alle 6 Repositories sind UNVERÄNDERT von PROMISE. Sie sind Spring-Data-JPA-Interfaces — Spring generiert die Implementierungen automatisch zur Laufzeit, kein Boilerplate-Code nötig.

| Datei | Verwaltet | Wichtige Methoden |
|---|---|---|
| `AgentRepository.java` | `Agent`-Entitäten | `findById(UUID)`, `save(Agent)`, `findAll()` — wird in jedem `AgentController`-Endpunkt aufgerufen |
| `StateRepository.java` | `State`-Entitäten | wird intern vom Cascade-Save genutzt |
| `StorageRepository.java` | `Storage`-Entitäten | analog |
| `StorageEntryRepository.java` | `StorageEntry`-Entitäten | analog |
| `UtteranceRepository.java` | `Utterance`-Entitäten | analog |
| `UtterancesRepository.java` | `Utterances`-Container | analog |

Bei einem `agentRepository.save(agent)`-Aufruf werden durch Hibernate-Kaskaden automatisch alle abhängigen Entitäten mit-gespeichert: States, Prompts, Transitions, Decisions, Actions, Utterances, StorageEntries. Ein Aufruf, eine Transaktion, alles oder nichts.

---

## 7. Backend — SPI (OpenAI-Schicht)

### `spi/LMOpenAI.java` (ANGEPASST)

Die zentrale Brücke zur OpenAI-API. Eine Klasse, fünf Methoden:

| Funktion | Aufgabe |
|---|---|
| `complete(Utterances, totalPrompt)` | Schickt System-Prompt + Konversationshistorie an `chat/completions` und gibt die Antwort als String zurück. Wird einmal pro `respond()`-Aufruf benutzt. |
| `decide(Utterances, decisionPrompt)` | Schickt die Konversation + einen Boolean-Prompt an die API und parst die Antwort als `true`/`false`. Für Guards. |
| `extract(Utterances, extractionPrompt)` | Schickt einen Extraktions-Prompt und erwartet eine JSON-Antwort, die ins Storage geschrieben wird. |
| `summarise(Utterances, summarisePrompt)` | Erzeugt eine **JSON**-Zusammenfassung (für strukturierte Datenextraktion). |
| `summariseOffline(Utterances)` (**NEU**) | Erzeugt eine **Plain-Text**-Zusammenfassung. Wird von `Utterances.compactIfNeeded()` aufgerufen — JSON würde dort in der Historie als merkwürdige Nachricht erscheinen. |

### `spi/OpenAIProperties.java` (UNVERÄNDERT)

Lädt `openai.url`, `openai.key`, `openai.model` aus der Properties-Datei (oder Environment-Variablen via `${...}`-Platzhalter).

### `spi/ContenFilterException.java` (UNVERÄNDERT)

Exception die `LMOpenAI` wirft falls OpenAI das Token-Limit überschreitet oder einen Content-Filter triggert.

### `spi/GsonExclude.java` (UNVERÄNDERT)

Custom-Annotation für Felder, die bei der JSON-Serialisierung mit Gson ausgeschlossen werden sollen (z.B. um Endlosschleifen bei bidirektionalen JPA-Relationen zu vermeiden).

### `utils/NamedParametersFormatter.java` (UNVERÄNDERT)

Hilfsklasse zum Ersetzen von Platzhaltern in Prompt-Strings. Z.B. `"Hallo {{nickname}}"` mit `Map.of("nickname", "Maria")` → `"Hallo Maria"`.

---

## 8. Backend — Properties & Ressourcen

### `application.properties.template` (Vorlage, NICHT im Repo committed)

Für lokale Entwicklung. Wird zu `application.properties` kopiert und mit echten Werten gefüllt.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/oblivio
spring.datasource.username=postgres
spring.datasource.password=...
spring.jpa.hibernate.ddl-auto=update
```

### `application-prod.properties` (NEU, ANGEPASST)

Produktions-Konfiguration. Wird aktiv wenn Railway `SPRING_PROFILES_ACTIVE=prod` setzt. Liest die Datenbank-Credentials aus Environment-Variablen (`${...}`-Platzhalter), damit das Passwort nie im Repo landet. Enthält zusätzlich PgBouncer-kompatible Hikari-Settings (`prepareThreshold=0`).

### `openai.properties.template` (Vorlage)

```properties
openai.url=https://api.openai.com/v1/chat/completions
openai.key=sk-...
openai.model=gpt-4o
```

### `openai-prod.properties` (NEU)

Produktions-Variante mit `openai.key=${OPENAI_KEY}`.

### `logback-spring.xml` (ANGEPASST)

Logback-Konfiguration. Definiert STDOUT-Logging plus den neuen `SseLogAppender` von Oblivio. Beide Appender hängen am Root-Logger.

### `src/main/resources/public/` (UNVERÄNDERT)

Statische HTML-Dateien aus dem PROMISE-Demo (index.html, monitor/, realtime/). Werden von Spring Boot direkt ausgeliefert. In Oblivio nicht aktiv genutzt — das produktive Frontend liegt auf Hostpoint. Hilfreich beim Debuggen lokal.

---

## 9. Frontend — HTML-Seiten

Alle Frontend-Dateien stehen in `Website-template/`. **Alle NEU für Oblivio** (PROMISE hatte nur Demo-HTML in `src/main/resources/public/`).

### `signup.html` / `login.html`

Authentifizierungs-Formulare. Inline-JavaScript am Seitenende initialisiert den Supabase-Client und ruft `supabase.auth.signUp()` bzw. `signInWithPassword()` direkt auf. Kein Backend-Aufruf nötig.

### `index.html`

Landing-Page. Marketing-Inhalte, Login-/Signup-Buttons, Sprach-Picker, Footer.

### `journey.html`

Dashboard nach Login. Zeigt die Personas des Users (via Supabase-Query auf `user_agents` + `legacy_access_codes`), bietet Buttons zum Weiterführen einer offenen Biographer-Session oder zum Anzeigen des Zugangscodes für die fertige Persona.

### `biographer.html` (~2700 Zeilen)

Die Biographer-UI. Drei Bereiche:

1. **Consent-Modal** — wird beim ersten Besuch angezeigt. `checkUserConsent()` / `saveUserConsent()` lesen/schreiben `user_consents`.
2. **Pre-Survey (Block 0)** — Multiple-Choice und Dropdown-Felder. `saveUserProfile()` schreibt den `nickname` nach `user_profiles` und alle Antworten als JSONB nach `questionnaire_answers`.
3. **Chat-UI** — Progressbar (1/10 bis 10/10), Chat-Bubbles, Eingabefeld, Voice-Input via Web Speech API. Bei Block-10-Confirm: Code-Generierung + Modal mit "Copy"-/"E-Mail"-Buttons.

Wichtige Funktionen: `createBiographer()` (in `biographer-promise.js` ausgelagert), `sendMessage()`, `loadConversationHistory()`, `showCompletionMessage()`, `generateAccessCode()`.

### `legacy.html` (~1500 Zeilen)

Die Persona-Chat-UI für Visitors. Drei Phasen:

1. **Code-Eingabe** — Form für den 8-stelligen Code, validiert per Supabase-Query auf `legacy_access_codes`.
2. **Visitor-Info** — Formular für Name, Beziehung, Geschlecht. Gespeichert nur in `localStorage` (kein Login).
3. **Chat-UI** — drei Variant-Buttons oben, Chat-Verlauf, Voice-Output (klickt der Visitor auf eine Persona-Antwort, wird `POST /{agentId}/tts` aufgerufen).

Wichtige Funktionen: `loadPersona()`, `switchMode()`, `sendMessage()`, `saveMessage()`, `loadConversationHistory()`, `getScopedVisitorId()`, `playAudio()`.

### Marketing-/Statische Seiten

`about.html`, `faq.html`, `blog.html`, `pricing.html`, `features.html`, `contact.html`, `privacy.html`, `terms.html`, `security.html`, `404.html` — statische Inhalte, kein dynamischer Code. Alle verwenden `translations.js` für die i18n.

---

## 10. Frontend — JavaScript-Module

### `js/translations.js`

Die i18n-Engine. Liest beim Page-Load `localStorage('oblivio_language')`, lädt das passende `lang-<code>.js`-File, wandert dann mit einem `MutationObserver` durch das DOM und ersetzt jeden `[data-i18n="key"]`-Eintrag mit dem entsprechenden Text. Auch dynamisch nachgeladene Elemente werden so übersetzt.

Wichtige Funktionen: `setLanguage(code)`, `t(key)`, `applyTranslations(root)`.

### `js/lang-<code>.js` (8 Dateien)

Sprachpakete für DE/EN/FR/IT/TR/KO/JA/ZH. Jede Datei ist ein einzelnes JavaScript-Objekt mit ~400 Key-Value-Paaren. Setzt `window.OBLIVIO_TRANSLATIONS[<code>] = { … }`.

### `js/biographer-promise.js` (~340 Zeilen)

API-Client für den Biographen. Kapselt alle Aufrufe ans Railway-Backend und alle Schreibzugriffe auf Supabase rund um die Biographer-Session.

| Funktion | Aufgabe |
|---|---|
| `createBiographer(nickname, language, userId)` | POSTet an `/agent/biographer`, schreibt das Ergebnis in `user_agents`, gibt die Agent-UUID zurück. |
| `getOrCreatePromiseAgent(userId, language, nickname)` | Prüft ob bereits ein passender Agent in `user_agents` existiert. Wenn ja: prüft via `GET /{agentId}/info` ob das Backend ihn noch kennt. Wenn nicht oder Nickname unterschiedlich: neuer Agent. |
| `sendMessage(agentId, content)` | POSTet an `/{agentId}/respond` und gibt die Antwort zurück. |
| `getState(agentId)` | GETtet `/{agentId}/state`. |
| `getStorage(agentId)` | GETtet `/{agentId}/storage` — die 10 Block-Summaries am Ende. |
| `resetPromiseConversation(agentId)` | DELETEt `/{agentId}/reset`. |
| `saveLegacyToSupabase(userId, agentId, legacyData)` | Schreibt die fertige Persona-Daten als JSONB in `user_legacies`. |
| `saveChatMessage(userId, agentId, role, content)` | Schreibt eine einzelne Nachricht in `chat_messages` (für UI-Rehydration bei Refresh). |
| `loadChatMessages(userId, agentId)` | Lädt die Historie aus `chat_messages` zurück in die UI. |
| `loadAllUserChatMessages(userId)` | Fallback: lädt alle Nachrichten eines Users — wird genutzt wenn PROMISE neu gestartet wurde und eine neue Agent-ID hat. |
| `saveBiographerConversation(...)` | Schreibt in `biographer_conversations` mit zusätzlichem Block-Kontext. |

### `js/legacy-chat.js` (~250 Zeilen)

API-Client für die Persona-Chats.

| Funktion | Aufgabe |
|---|---|
| `buildVisitorContext(visitorInfo, language)` | Erzeugt einen mehrsprachigen Visitor-Kontext-Block ("Du sprichst mit Maria, der Tochter der Persona, weiblich, …") der in den System-Prompt eingefügt wird. |
| `buildLegacySystemPrompt(legacyData, mode, visitorInfo, language)` | Kombiniert das passende `full_prompt_<mode>` mit dem Visitor-Kontext. |
| `createLegacyAgent(systemPrompt, starterPrompt, userId)` | POSTet an `/agent/singlestate` mit den Prompts. |
| `getScopedVisitorId(mode)` | Hängt `__active` / `__passive` / `__analysis` an die Visitor-UUID an, damit jede Variante eine eigene Historie in `legacy_messages` bekommt. |
| `sendLegacyMessage(agentId, content)` | POSTet an `/{agentId}/respond`. |
| `requestTTS(agentId, text, voiceId)` | POSTet an `/{agentId}/tts`, gibt einen `Blob` mit MP3-Daten zurück. |

### `js/config.js.template` (Vorlage)

Liefert die Endpunkte für den Browser:

```javascript
window.OBLIVIO_CONFIG = {
    PROMISE_API_URL: 'https://your-promise-backend.example.com',
    SUPABASE_URL: 'https://your-project.supabase.co',
    SUPABASE_ANON_KEY: 'your-anon-key-here'
};
```

Wird zu `config.js` umbenannt und mit echten Werten gefüllt.

---

## 11. Datenbank — SQL-Skripte

### `sql/SUPABASE_TABLES.sql`

Einmaliges Setup-Skript. Legt `user_agents` und `user_legacies` an, aktiviert RLS, definiert die Policies. **Wichtig:** Erstellt nur die zwei Kern-Tabellen — die übrigen 7 (`user_profiles`, `user_consents`, `questionnaire_answers`, `chat_messages`, `biographer_conversations`, `legacy_access_codes`, `legacy_messages`) sind in der README-Schema-Sektion dokumentiert und müssen ergänzend angelegt werden.

### `sql/supabase_migrations.sql`

Migrations-Skript mit erweiterter Funktionalität: enthält zusätzlich `is_shared` und `share_token` auf `user_legacies` für öffentliches Teilen, plus Performance-Indexe.

---

## 12. Infrastruktur — Docker, Railway, Maven

### `Dockerfile` (NEU)

Multi-Stage-Build für Railway. **Stage 1** (Build): `eclipse-temurin:21-jdk` lädt Maven-Dependencies und baut das JAR. **Stage 2** (Runtime): `eclipse-temurin:21-jre` kopiert nur das fertige JAR — viel kleineres Image (~200 MB statt ~700 MB). Container läuft als `nobody`-User auf Port 8080.

### `railway.json` (NEU)

Railway-spezifische Konfiguration. Sagt Railway: bauen via Dockerfile, starten mit `java -jar app.jar`, Health-Check unter `/actuator/health` alle paar Sekunden, automatischer Neustart bei Crash.

### `.railwayignore` (NEU)

Liste was Railway beim Build ignorieren soll: `target/`, `.git/`, `docs/`, `sql/`, `*.md`. Spart Build-Zeit und Image-Grösse.

### `.gitignore` (ANGEPASST)

PROMISE-Original + Oblivio-Ergänzungen (`Personas/`, `*.docx`, `application.properties`, `openai.properties`).

### `.env.example` (NEU)

Vorlage für lokale `.env`-Datei mit allen benötigten Variablen: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `OPENAI_KEY`, `ELEVENLABS_API_KEY`, `SPRING_PROFILES_ACTIVE`.

### `pom.xml` (ANGEPASST)

Maven-Build-Konfiguration. **Oblivio-Änderungen:** MySQL-Driver entfernt, `org.postgresql:postgresql` hinzugefügt (für Supabase). Sonst Stock-PROMISE: Spring-Boot-Starter Web + JPA + Actuator, Gson, OpenAI-Properties.

### `mvnw` / `mvnw.cmd` / `.mvn/wrapper/` (UNVERÄNDERT)

Maven Wrapper. Lädt sich beim ersten Lauf selbst Maven herunter — der Container braucht keine Maven-Installation.

### `CITATION.cff`

Maschinenlesbare Zitierungsmetadaten. GitHub zeigt darauf basierend automatisch einen "Cite this repository"-Button an.

### `LICENSE`

Academic License — restriktivere Eigenlizenz für Bachelorarbeits-Kontext.

---

## Anhang — Welche Dateien sind NICHT hier dokumentiert?

Dieses Dokument deckt die **Oblivio-relevanten** Dateien ab. Bewusst ausgelassen wurden:

- **`controllers/AgentControllerRealtime.java`, `RealtimeController.java`, `RealtimeSessionView.java`, `spi/RealtimeSessionClient.java`, `RealtimeSessionInfo.java`** — experimentelle OpenAI-Realtime-API-Integration (Voice-to-Voice), in Oblivio derzeit nicht aktiv genutzt. Liegt im Repo für mögliche Weiterentwicklung.
- **`controllers/StaticRedirectController.java`** — Hilfs-Controller für statische Pfad-Redirects auf das interne PROMISE-Demo-Frontend (`/monitor`, `/realtime`). Für Oblivio nicht relevant.
- **`controllers/views/PromptResponseView.java`** — PROMISE-Demo-Response, in Oblivio ungenutzt.
- **`model/commons/states/`** (14 spezialisierte State-Klassen) — alle PROMISE-Demos, in Oblivio nicht verwendet.
- **`src/test/`** — PROMISE-Test-Bots (`SingleStateInteraction`, `MultiStateInteraction`, `MultiLayeredInteraction`, `TwoStatesInteraction`, …). Im Dockerfile mit `-DskipTests` übersprungen.

Wer diese Dateien ebenfalls dokumentiert haben möchte, kann sich am Muster oben orientieren oder das README-Repo-Kapitel zu PROMISE Commons lesen.

---

**Autor:** Dennis Riccardo Dewiri
**ZHAW School of Management and Law · Business Informatics · 2026**
