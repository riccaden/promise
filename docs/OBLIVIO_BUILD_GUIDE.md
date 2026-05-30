# Oblivio Build Guide

Vollständige technische Dokumentation und Nachbauanleitung für die Oblivio-Plattform — eine KI-gestützte Anwendung für digitale Vermächtnisse, aufgebaut auf dem PROMISE-Framework der ZHAW.

> **Diese Anleitung richtet sich an Personen, die noch nie mit PROMISE gearbeitet haben.** Sie erklärt zuerst PROMISE selbst, dann Schritt für Schritt jede Erweiterung, die Oblivio gebaut hat — inklusive Begründung warum sie nötig war und was sie genau tut.

---

## Inhaltsverzeichnis

1. [Projektüberblick](#1-projektüberblick)
2. [Was läuft wo — die Deployment-Matrix](#2-was-läuft-wo--die-deployment-matrix)
3. [Architekturdiagramm](#3-architekturdiagramm)
4. [PROMISE-Framework — eine Einführung](#4-promise-framework--eine-einführung)
5. [Wie Oblivio PROMISE konkret nutzt](#5-wie-oblivio-promise-konkret-nutzt)
6. [Erweiterungen am PROMISE-Framework](#6-erweiterungen-am-promise-framework)
7. [Komponenten auf Railway (Java-Backend)](#7-komponenten-auf-railway-java-backend)
8. [Komponenten auf Hostpoint (Frontend)](#8-komponenten-auf-hostpoint-frontend)
9. [Komponenten auf Supabase (DB + Auth)](#9-komponenten-auf-supabase-db--auth)
10. [Externe APIs (OpenAI + ElevenLabs)](#10-externe-apis-openai--elevenlabs)
11. [Der Biographer im Detail](#11-der-biographer-im-detail)
12. [Der Legacy-Chat im Detail](#12-der-legacy-chat-im-detail)
13. [Mehrsprachigkeit](#13-mehrsprachigkeit)
14. [Visitor-Tracking](#14-visitor-tracking)
15. [Persona-Prompt-Architektur](#15-persona-prompt-architektur)
16. [TTS-Integration](#16-tts-integration)
17. [Datenbank-Schema](#17-datenbank-schema)
18. [Schritt-für-Schritt: Oblivio von Null nachbauen](#18-schritt-für-schritt-oblivio-von-null-nachbauen)
19. [Datei-Index: Was steht wo](#19-datei-index-was-steht-wo)
20. [Bekannte Limitationen](#20-bekannte-limitationen)

---

## 1. Projektüberblick

Oblivio ist eine Web-Plattform, die es Menschen ermöglicht, ihre Lebensgeschichte über zehn thematische Gesprächsblöcke mit einem KI-Biografen zu dokumentieren. Aus den extrahierten Inhalten wird eine interaktive Persona generiert, mit der Angehörige in Dialog treten können — in drei verschiedenen Konversationsvarianten.

Das Projekt wurde als Bachelorarbeit an der ZHAW realisiert und baut auf dem dort entwickelten PROMISE-Framework auf.

### Kennzahlen
- **21 States** im Biographer (10 Blöcke × 2 + Final)
- **8 unterstützte Sprachen** (DE, EN, FR, IT, TR, KO, JA, ZH)
- **3 Konversationsvarianten** (active, passive, analysis)
- **15 Studienteilnehmende** wurden zu Personas verarbeitet
- **70+ Java-Klassen** im Backend
- **16 HTML-Seiten** im Frontend

---

## 2. Was läuft wo — die Deployment-Matrix

Oblivio besteht aus **drei physisch getrennten Hosting-Komponenten** und **zwei externen APIs**:

| Was | Wo | URL | Was es tut |
|---|---|---|---|
| **Frontend (Static)** | Hostpoint (Schweiz) | https://oblivio.ch | UI, i18n, Auth-Flow, Persona-Mode-UI |
| **Backend (Java)** | Railway (Container) | https://promise-production.up.railway.app | PROMISE State Machine, REST-API |
| **Datenbank + Auth** | Supabase (Cloud) | https://<project>.supabase.co | PostgreSQL, JWT-Auth, RLS |
| **LLM** | OpenAI API | https://api.openai.com | GPT-4o-Calls |
| **TTS** | ElevenLabs API | https://api.elevenlabs.io | Voice-Synthese |

### Verantwortlichkeits-Tabelle

| Aktion | Frontend | Backend | Supabase | OpenAI | ElevenLabs |
|---|:-:|:-:|:-:|:-:|:-:|
| User-Registrierung | ✓ | — | ✓ | — | — |
| Block 0 (Pre-Survey) | ✓ | — | ✓ | — | — |
| Biographer starten | ✓ | ✓ | ✓ | — | — |
| Biographer-Frage stellen | — | ✓ | ✓ | ✓ | — |
| Block-Summary extrahieren | — | ✓ | ✓ | ✓ | — |
| Persona-Prompt erzeugen | — | (manuell) | ✓ | — | — |
| Legacy-Chat starten | ✓ | ✓ | ✓ | ✓ | — |
| Persona antwortet | — | ✓ | ✓ | ✓ | — |
| Voice-Wiedergabe | ✓ | ✓ | — | — | ✓ |
| Variante wechseln | ✓ | ✓ | ✓ | — | — |
| Context-Compaction | — | ✓ | ✓ | ✓ | — |
| Nachrichten persistieren | ✓ | ✓ | ✓ | — | — |

---

## 3. Architekturdiagramm

```
                            ┌──────────────────────────────────────────────┐
                            │           BROWSER (User-Device)              │
                            │  - Vanilla JavaScript                        │
                            │  - localStorage (Visitor-IDs, Mode, Sprache) │
                            └──────────────┬───────────────────────────────┘
                                           │ HTTPS GET/POST
                ┌──────────────────────────┼──────────────────────────┐
                ▼                          ▼                          ▼
       ┌───────────────────┐   ┌───────────────────────┐    ┌───────────────────┐
       │  HOSTPOINT (CH)   │   │  RAILWAY (Container)  │    │  SUPABASE (Cloud) │
       │  - oblivio.ch     │   │  - Java 21 + Spring   │    │  - PostgreSQL     │
       │  - HTML/CSS/JS    │   │  - PROMISE Framework  │    │  - Auth (JWT)     │
       │  - Bilder         │   │  - REST-API           │    │  - RLS Policies   │
       │  - Avatare        │   │  - Docker Multi-Stage │    │                   │
       │                   │   │                       │    │  Tabellen:        │
       │  Manueller Upload │   │  Auto-Deploy on push  │    │  user_agents      │
       └───────────────────┘   └───────────┬───────────┘    │  user_legacies    │
                                           │ JDBC           │  legacy_access_   │
                                           ▼                │      codes        │
                                ┌───────────────────────┐   │  legacy_messages  │
                                │  Hibernate verwaltet  │◄──┤                   │
                                │  PROMISE-Tabellen     │   │  + PROMISE-Tab.   │
                                │  in SELBER Supabase   │   │  (agent, state,   │
                                │  PostgreSQL-Instanz   │   │   utterance, ...) │
                                └───────────────────────┘   └───────────────────┘
                                                            
                          ┌──────────────────────────────────────┐
                          ▼ HTTPS (aus Backend)                  ▼ HTTPS (aus Backend)
                  ┌────────────────────┐               ┌────────────────────┐
                  │   OpenAI API       │               │   ElevenLabs API   │
                  │   GPT-4o           │               │   TTS              │
                  └────────────────────┘               └────────────────────┘
```

**Wichtige Beobachtung:** Sowohl die Oblivio-Tabellen als auch die PROMISE-eigenen Tabellen liegen in **derselben Supabase-PostgreSQL-Datenbank**. Es gibt keine zweite Datenbank.

---

## 4. PROMISE-Framework — eine Einführung

PROMISE ist ein Open-Source-Framework, das an der ZHAW entwickelt wurde (https://github.com/2024-ZHAW-PM4). Es löst ein zentrales Problem beim Bau von KI-Konversationen: **Wie strukturiert man ein Gespräch, das mehrere Phasen hat, Daten extrahieren soll und nicht aus dem Ruder läuft?**

### 4.1 Das Grundproblem ohne PROMISE

Stellen wir uns vor, jemand baut einen KI-Biografen ohne Framework. Naheliegende Lösung: ein einziger System-Prompt à la *"Du bist ein Biograf. Stelle Fragen zu Geschmack, dann Alltag, dann Erinnerungen..."*. Das funktioniert für 10 Minuten — aber dann:

- Die KI vergisst, wo sie war
- Sie überspringt Themen
- Sie redet über alles auf einmal
- Sie merkt nicht, wann ein Thema "fertig" ist
- Sie speichert keine strukturierten Daten

PROMISE löst das, indem es Gespräche als **endliche Zustandsautomaten** modelliert — eine Idee aus der Informatik der 1950er, angewendet auf moderne LLMs.

### 4.2 Die zentrale Idee: Conversation als State Machine

Statt eines einzigen riesigen Prompts gibt es viele kleine **States** (Zustände). Jeder State hat:

- **Einen eigenen System-Prompt** (z.B. *"Du bist Biograf, fokussiere dich jetzt nur auf das Thema Erinnerungen"*)
- **Einen Gesprächsverlauf** (was bisher in diesem State gesagt wurde)
- **Übergänge zu anderen States** (Transitions)

Die KI wechselt von einem State zum nächsten, sobald bestimmte Bedingungen erfüllt sind. So bleibt jede Phase fokussiert, ohne dass die KI "vergisst" oder durcheinanderkommt.

### 4.3 Die fünf Kernkonzepte von PROMISE

Wenn du PROMISE verstehen willst, musst du fünf Begriffe kennen:

#### (1) Prompt
Die abstrakte Basisklasse. Ein Prompt ist im Grunde nur "ein Stück Text, das an die KI geschickt werden kann". Sowohl States als auch Decisions und Actions sind technisch gesehen Prompts (sie erben davon). Das macht das Speichern in der Datenbank einheitlich.

**Datei:** [`model/Prompt.java`](../src/main/java/ch/zhaw/statefulconversation/model/Prompt.java)

#### (2) State
Ein Gesprächszustand. Hat einen System-Prompt (Persönlichkeit und Aufgabe der KI in dieser Phase), eine Starter-Nachricht (was die KI als Erstes sagt, wenn sie diesen State betritt), eine Liste von Transitions (mögliche Übergänge zu anderen States) und ein Utterances-Objekt (alle bisher gesprochenen Nachrichten in diesem State).

Im Wesentlichen sagt ein State: *"Wenn du gerade hier bist, dann verhalte dich so, sammle das, und wechsle weiter, sobald X erfüllt ist."*

**Datei:** [`model/State.java`](../src/main/java/ch/zhaw/statefulconversation/model/State.java)

#### (3) Transition (Übergang)
Die Verbindung zwischen zwei States. Eine Transition hat drei Bestandteile:

- **Decisions (Guards):** Bedingungen, die erfüllt sein müssen, damit der Übergang stattfindet. Werden als UND-Liste evaluiert.
- **Actions:** Was passieren soll *beim* Übergang. Werden sequentiell ausgeführt.
- **Subsequent State:** Wohin der Übergang führt.

Beispiel: *"Wechsle vom Conv-State zum Confirm-State, wenn alle 11 Fragen gestellt wurden (Guard), und kopiere dabei die Utterances in den nächsten State (Action)."*

**Datei:** [`model/Transition.java`](../src/main/java/ch/zhaw/statefulconversation/model/Transition.java)

#### (4) Decision (Guard)
Eine Bedingung, die das LLM mit `true` oder `false` beantwortet. Bei einem Conv-State wäre ein typischer Guard: *"Hat der Biograf alle 11 vordefinierten Fragen gestellt? Antworte nur mit true oder false."*

Die Decision sendet den Gesprächsverlauf + die Frage an GPT-4o, der entweder `true` oder `false` zurückgibt. Java parst das Ergebnis mit `Boolean.parseBoolean()`.

**Wichtig:** Es gibt zwei Arten von Decisions:
- **StaticDecision:** Fester Prompt-Text, der direkt verwendet wird
- **DynamicDecision:** Prompt wird zur Laufzeit aus dem Storage zusammengebaut (für komplexere Fälle)

**Dateien:** [`model/Decision.java`](../src/main/java/ch/zhaw/statefulconversation/model/Decision.java), [`commons/decisions/StaticDecision.java`](../src/main/java/ch/zhaw/statefulconversation/model/commons/decisions/StaticDecision.java)

#### (5) Action (Aktion)
Ein Seiteneffekt, der beim Übergang ausgeführt wird. PROMISE liefert mehrere Action-Typen mit:

- **StaticExtractionAction:** Lässt die KI strukturierte Daten aus dem Gespräch extrahieren (z.B. eine JSON-Zusammenfassung) und speichert sie im Storage.
- **TransferUtterancesAction:** Kopiert den Gesprächsverlauf von einem State in einen anderen.
- **StaticSummarisationAction:** Wie Extraction, aber ohne JSON-Format.
- **RemoveLastUtteranceAction:** Entfernt die letzte User-Nachricht.

**Datei:** [`model/Action.java`](../src/main/java/ch/zhaw/statefulconversation/model/Action.java) + alle Dateien in [`commons/actions/`](../src/main/java/ch/zhaw/statefulconversation/model/commons/actions/)

#### (6) Agent
Der Container, der alles zusammenhält. Ein Agent hat einen `initialState` (wo das Gespräch startet), einen `currentState` (wo es gerade ist) und einen `storage` (Key-Value-Speicher für extrahierte Daten).

**Datei:** [`model/Agent.java`](../src/main/java/ch/zhaw/statefulconversation/model/Agent.java)

### 4.4 Wie ein Gespräch in PROMISE technisch abläuft

Hier der konkrete Ablauf einer User-Nachricht:

```
1. Frontend → POST /{agentId}/respond { "content": "Mein Lieblingsessen ist Pasta" }
2. AgentController.respond() → lädt Agent aus DB
3. Agent.respond("Mein Lieblingsessen ist Pasta")
   ↓
4. State.respond(userSays)
   ↓
5. State.acknowledge(userSays):
   - Fügt die User-Nachricht zu utterances hinzu
   - Ruft raiseIfTransit() auf
   ↓
6. raiseIfTransit() iteriert durch alle Transitions:
   ↓
7. Für jede Transition: Transition.decide(utterances):
   - Iteriert durch Decisions
   - Bei jeder Decision: LMOpenAI.decide(utterances, decisionPrompt) → GPT-Call → true/false
   - Wenn ALLE Decisions true → Transition feuert
   ↓
8. Falls Transition feuert: Transition.action(utterances):
   - Führt alle Actions aus (z.B. StaticExtractionAction extrahiert Daten und speichert sie)
   - Wirft TransitionException
   ↓
9. Agent.respond() fängt die TransitionException:
   - Setzt currentState = subsequentState
   - Ruft respond() rekursiv im neuen State auf
   ↓
10. Falls KEINE Transition feuert: State.respond() generiert normale Antwort:
    - composeTotalPrompt() baut System-Prompt zusammen
    - LMOpenAI.complete(utterances, totalPrompt) → GPT-Call → Antwort
    - Antwort wird zu utterances hinzugefügt
    - Antwort wird zurückgegeben
    ↓
11. AgentController gibt Response an Frontend zurück
```

### 4.5 Die LMOpenAI-Klasse (das Bindeglied zu GPT)

PROMISE kommuniziert mit GPT-4o ausschliesslich über eine statische Hilfsklasse [`LMOpenAI`](../src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java). Diese hat fünf zentrale Methoden:

| Methode | Zweck | Beispiel |
|---|---|---|
| `complete()` | Generiert eine normale Assistent-Antwort | Conv-State antwortet dem User |
| `decide()` | Erwartet `true`/`false` zurück | Guard prüft ob Bedingung erfüllt ist |
| `extract()` | Extrahiert strukturierte JSON-Daten | Block-Summary erstellen |
| `summarise()` | Fasst Gespräch als JSON zusammen | für Storage |
| `summariseOffline()` | Fasst Gespräch als Plain-Text zusammen | für Context Compaction (von Oblivio hinzugefügt) |

Jede Methode baut intern einen Prompt aus dem Gesprächsverlauf + einer System-Anweisung zusammen, schickt ihn an die OpenAI-API und parst das Ergebnis.

### 4.6 Persistierung über JPA/Hibernate

Alles wird automatisch in der Datenbank gespeichert. PROMISE nutzt **JPA** (Java Persistence API) mit **Hibernate** als Implementation. Das heisst:

- Jede `@Entity`-Klasse (Agent, State, Prompt, Utterance, Storage, ...) wird zu einer Tabelle
- Hibernate erstellt die Tabellen automatisch beim ersten Start (`ddl-auto=update`)
- Beim `repository.save(agent)` wird der gesamte Agent inklusive aller verbundenen States, Utterances etc. gespeichert
- Die `@OneToMany`-Beziehungen werden über Fremdschlüssel abgebildet

### 4.7 Was PROMISE NICHT macht

Wichtig zu verstehen, was PROMISE **nicht** abdeckt:

- ❌ **Kein Frontend** — PROMISE ist nur Backend (Java + REST-API)
- ❌ **Keine Authentifizierung** — keine User-Management, keine Logins
- ❌ **Keine Multi-User-Trennung** — alle Agents teilen sich die gleiche Datenbank ohne RLS
- ❌ **Keine Mehrsprachigkeit** — alles Englisch (oder was auch immer im Prompt steht)
- ❌ **Keine TTS** — nur Text-Output
- ❌ **Keine Token-Optimierung** — bei langen Gesprächen wachsen die Kosten linear
- ❌ **Keine Anwendungslogik** — du musst selbst entscheiden, was deine States bedeuten

Genau hier setzt Oblivio an: alles, was PROMISE nicht bietet, musste gebaut werden.

---

## 5. Wie Oblivio PROMISE konkret nutzt

Bevor wir uns die Erweiterungen ansehen, hier die konkrete Verwendung der PROMISE-Bausteine in Oblivio:

### 5.1 Zwei verschiedene Agent-Typen

Oblivio nutzt PROMISE auf zwei sehr unterschiedliche Weisen:

#### (A) Biographer-Agent (komplex: 21 States)
Der KI-Biograf, der die Lebensgeschichte aufnimmt. Hier wird die State-Machine-Idee voll ausgenutzt: 10 Themenblöcke à 2 States (Gespräch + Bestätigung) + 1 Final-State.

**Aufbau-Datei:** [`AgentMetaUtility.createBiographerAgent()`](../src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64)

#### (B) Legacy-Agent (einfach: 1 State)
Die Persona, mit der Angehörige chatten. Hier wird PROMISE quasi nur als "Hülle" verwendet — es gibt nur einen einzigen State + einen Final-State.

**Aufbau-Datei:** [`AgentMetaUtility.createSingleStateAgent()`](../src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L20)

### 5.2 Mapping zwischen PROMISE-Konzepten und Oblivio-Inhalten

| PROMISE-Konzept | Im Biographer-Agent | Im Legacy-Agent |
|---|---|---|
| **State** | Ein Themenblock (z.B. "Erinnerungen") oder dessen Bestätigungs-Phase | Die ganze Persona |
| **Transition.decide()** | "Wurden alle Fragen gestellt?" oder "Hat User bestätigt?" | "Verabschiedet sich User?" |
| **Transition.action()** | TransferUtterances (Conv→Confirm) oder StaticExtraction (Block-Summary) | StaticExtraction (Gesamt-Summary) |
| **Storage** | Speichert `block1` bis `block10` (Block-Summaries) | Speichert `summary` (Gesprächs-Zusammenfassung) |
| **Final-State** | "Biografie abgeschlossen" | Verabschiedungs-State |

### 5.3 Konkretes Beispiel: Block 1 des Biographers

```
┌─────────────────────────────────────────────────────────┐
│  State: "Block 1 - Ice-Breaker"                         │
│  ├─ System-Prompt: "Du bist ein Biograf. Stelle 11      │
│  │  Fragen zu Geschmack & Vorlieben..."                 │
│  ├─ Starter-Prompt: "Begrüsse warmherzig..."            │
│  └─ Transition zum nächsten State:                      │
│     ├─ Decision (StaticDecision):                       │
│     │  "Hat der Biograf alle 11 Fragen gestellt?        │
│     │   Antworte nur true oder false."                  │
│     └─ Action (TransferUtterancesAction):               │
│        Kopiert Utterances ins nächste State             │
└─────────────────────────────────────────────────────────┘
                          │ (wenn Decision true)
                          ▼
┌─────────────────────────────────────────────────────────┐
│  State: "Block 1 - Ice-Breaker - Bestätigung"           │
│  ├─ System-Prompt: "Fasse zusammen was du erfahren      │
│  │  hast, frage nach Bestätigung..."                    │
│  └─ Transition zum nächsten State:                      │
│     ├─ Decision: "Hat User bestätigt?"                  │
│     └─ Action (StaticExtractionAction):                 │
│        Extrahiert strukturierte JSON-Zusammenfassung    │
│        und speichert sie unter Storage["block1"]        │
└─────────────────────────────────────────────────────────┘
                          │ (wenn bestätigt)
                          ▼
                  Block 2 ... usw.
```

---

## 6. Erweiterungen am PROMISE-Framework

Hier kommt der wichtigste Teil: Was musste Oblivio konkret am PROMISE-Code ändern und ergänzen — und warum?

### 6.1 ÜBERSICHT: Was wurde wo verändert

| Kategorie | Anzahl Dateien | Beschreibung |
|---|---|---|
| **Unverändert übernommen** | ~30 Dateien | Kern-Klassen wie Agent, Transition, Storage etc. |
| **Leicht modifiziert** | 5 Dateien | DB-Spalten auf TEXT, kleine Methoden hinzugefügt |
| **Stark erweitert** | 2 Dateien | `Utterances.java` (Compaction), `AgentMetaUtility.java` (Biographer) |
| **Komplett neu** | 11 Dateien | TTS, Multi-User, Logging-Pipeline, CORS |

### 6.2 Erweiterung 1: Context Compaction in Utterances.java

#### WARUM wurde diese Erweiterung gebaut?

Im Original-PROMISE wächst der Gesprächskontext bei jeder neuen Nachricht. Bei einem Biographer-Block mit 50 Nachrichten wird also bei jeder weiteren Nachricht der **gesamte** Verlauf erneut an GPT-4o geschickt. Das hat zwei Probleme:

1. **Token-Kosten:** Bei GPT-4o kostet jeder Input-Token Geld. Ein 50-Nachrichten-Gespräch sendet ~5000 Tokens pro Anfrage — multipliziert mit jeder neuen Nachricht.
2. **Kontext-Limit:** GPT-4o hat ein Kontextfenster (128k Tokens). Bei sehr langen Gesprächen würde das gesprengt.

Die Lösung in Oblivio: Nach 20 User-Nachrichten werden die älteren Nachrichten **zusammengefasst** und durch eine kurze System-Message ersetzt. So bleibt der Gesprächskontext kompakt.

#### WAS macht die Funktion genau?

[`Utterances.compactIfNeeded()`](../src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) (61 Zeilen):

1. **Zählt** die User-Nachrichten in der Liste
2. **Prüft**: Wenn ≤ 20, mache nichts und return
3. **Prüft**: Wenn die erste Message bereits eine Kompaktierungs-System-Message ist (am Prefix `[Zusammenfassung des bisherigen Gesprächs]` erkennbar), return — wir kompaktieren nicht doppelt
4. **Splittet** die Nachrichten: Die letzten 10 bleiben, alles davor wird zusammengefasst
5. **Erzeugt eine temporäre Utterances**, in die alle alten Nachrichten als Text-Block kopiert werden
6. **Ruft `LMOpenAI.summariseOffline()` auf** mit einem Prompt wie: *"Fasse das Gespräch in 3-5 Sätzen zusammen. Behalte die wichtigsten Themen, gestellten Fragen und emotionalen Momente."*
7. **Erhält** eine zusammenfassende Text-Antwort von GPT
8. **Löscht** die alten Nachrichten aus der Liste (mittels JPA-orphanRemoval)
9. **Fügt** eine neue System-Message ganz vorne ein: `[Zusammenfassung des bisherigen Gesprächs]\n<summary>`

#### WO wird die Funktion aufgerufen?

In [`State.respond()`](../src/main/java/ch/zhaw/statefulconversation/model/State.java#L171) — genau eine Code-Zeile wurde dort eingefügt:

```java
public Response respond(String userSays, String outerPrompt) throws TransitionException {
    this.acknowledge(userSays, outerPrompt);
    this.utterances.compactIfNeeded();  // ← Diese Zeile ist neu in Oblivio
    String totalPrompt = this.composeTotalPrompt(outerPrompt);
    // ...
}
```

#### ZUSATZ: Neue LMOpenAI-Methode

Damit die Kompaktierung funktioniert, brauchte es eine neue LMOpenAI-Methode: [`summariseOffline()`](../src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java). Im Gegensatz zu `summarise()` (gibt JSON zurück) gibt diese Plain-Text zurück, der direkt als System-Message verwendet werden kann.

#### Konstanten
```java
private static final int USER_MESSAGE_COMPACT_THRESHOLD = 20;  // Schwellwert
private static final int MESSAGES_TO_KEEP = 10;                 // Wie viele bleiben
```

---

### 6.3 Erweiterung 2: Biographer-Factory in AgentMetaUtility.java

#### WARUM wurde diese Erweiterung gebaut?

PROMISE liefert keine vorgefertigten Agent-Typen. Es liefert nur die Bausteine (State, Transition, Decision, Action). Das Konkrete — *"baue einen Biographer mit 10 thematischen Blöcken"* — muss man selbst implementieren.

Das ist sehr aufwendig, denn:
- Jeder der 10 Blöcke braucht **7 verschiedene Prompts** (Conv-System, Conv-Starter, Conv-Guard, Confirm-System, Confirm-Starter, Confirm-Guard, Extract-Prompt)
- Jeder Prompt muss in **8 Sprachen** funktionieren
- Die State-Kette muss **rückwärts** aufgebaut werden, damit jeder State auf seinen Nachfolger zeigen kann

#### WAS macht die Funktion genau?

[`AgentMetaUtility.createBiographerAgent()`](../src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64) (~50 Zeilen):

```java
public static Agent createBiographerAgent(BiographerAgentCreateDTO data) {
    var storage = new Storage();
    String language = data.getLanguage() != null ? data.getLanguage() : "de";
    String nickname = data.getNickname();

    String[][] prompts = buildBlockPrompts(language, nickname);
    String[] blockNames = buildBlockNames();

    // Startet bei Final-State und baut rückwärts auf
    State current = new Final("Biografie abgeschlossen",
                              getFinalPrompt(language),
                              getFinalStarterPrompt(language, nickname));

    for (int i = 9; i >= 0; i--) {
        State nextState = current;
        String storageKey = "block" + (i + 1);

        // Confirm-State:
        Decision confirmGuard = new StaticDecision(prompts[i][5]);
        Action extract = new StaticExtractionAction(prompts[i][6], storage, storageKey);
        Transition confirmTransition = new Transition(
            List.of(confirmGuard), List.of(extract), nextState);
        State confirmState = new State(
            prompts[i][3], blockNames[i] + " - Bestätigung", prompts[i][4],
            List.of(confirmTransition));

        // Conv-State:
        Decision convGuard = new StaticDecision(prompts[i][2]);
        Action transferToConfirm = new TransferUtterancesAction(confirmState);
        Transition convTransition = new Transition(
            List.of(convGuard), List.of(transferToConfirm), confirmState);
        State convState = new State(
            prompts[i][0], blockNames[i], prompts[i][1], List.of(convTransition));

        current = convState;
    }

    Agent result = new Agent(data.getAgentName(), data.getAgentDescription(), current, storage);
    result.start();
    return result;
}
```

#### Warum rückwärts?

Weil bei PROMISE eine Transition immer einen `subsequentState` braucht — der zum Zeitpunkt der Erstellung schon existieren muss. Deshalb startet man bei Block 10 (bzw. Final) und baut rückwärts auf.

#### Begleitfunktionen

- `buildBlockPrompts(language, nickname)`: Erzeugt das 2D-Array `prompts[10][7]` mit allen 70 Prompts
- `buildBlockNames()`: Liefert die 10 Block-Namen
- `getLanguageInstruction(language)`: Sprach-Präfix (z.B. *"...auf Türkisch kommunizieren..."*)
- `getFinalPrompt(language)`: System-Prompt für den Final-State
- `getFinalStarterPrompt(language, nickname)`: Abschiedsformel

---

### 6.4 Erweiterung 3: TEXT-Spalten in der Datenbank

#### WARUM wurde diese Erweiterung gebaut?

PROMISE definiert die Prompt-Spalte als `@Column(length = 10000)` — also VARCHAR(10000). Die Persona-Prompts in Oblivio sind 12000-20000 Zeichen lang (mit allen Sektionen). Bei zu langem Prompt würde PostgreSQL einen `value too long` Fehler werfen.

#### WAS wurde konkret geändert?

| Datei | Spalte | Vorher | Nachher |
|---|---|---|---|
| [`model/Prompt.java`](../src/main/java/ch/zhaw/statefulconversation/model/Prompt.java) | `prompt` | VARCHAR(10000) | TEXT |
| [`model/State.java`](../src/main/java/ch/zhaw/statefulconversation/model/State.java) | `starterPrompt` | VARCHAR(10000) | TEXT |
| [`model/State.java`](../src/main/java/ch/zhaw/statefulconversation/model/State.java) | `summarisePrompt` | VARCHAR(10000) | TEXT |
| [`model/Utterance.java`](../src/main/java/ch/zhaw/statefulconversation/model/Utterance.java) | `content` | VARCHAR(4096) | TEXT |

PostgreSQLs TEXT-Typ hat **keine Grössenbeschränkung**.

#### Stolperstein: Hibernate ändert keine Spaltentypen

`ddl-auto=update` erweitert Tabellen automatisch um neue Spalten, ändert aber **nicht** den Typ bestehender Spalten. Beim Migrieren musste manuell auf der DB gefahren werden:

```sql
ALTER TABLE prompt ALTER COLUMN prompt TYPE TEXT;
ALTER TABLE state ALTER COLUMN starter_prompt TYPE TEXT;
ALTER TABLE state ALTER COLUMN summarise_prompt TYPE TEXT;
ALTER TABLE utterance ALTER COLUMN content TYPE TEXT;
```

---

### 6.5 Erweiterung 4: TTS-Controller (komplett neu)

#### WARUM wurde diese Erweiterung gebaut?

Oblivio sollte den Personas eine **Stimme** geben. PROMISE hat keine TTS-Funktionalität, also musste eine Bridge zur ElevenLabs-API gebaut werden.

#### WAS macht der Controller genau?

[`TTSController.java`](../src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) (~100 Zeilen) — komplett neue Datei:

1. **Empfängt** `POST /{agentID}/tts?voice_id=xyz` mit JSON-Body `{ "text": "Hallo, wie geht's?" }`
2. **Wählt** die Voice-ID
3. **Baut** die ElevenLabs-API-URL
4. **Sendet** einen POST-Request mit `text`, `model_id: eleven_multilingual_v2`, `voice_settings`
5. **Empfängt** MP3-Bytes
6. **Gibt** sie an das Frontend zurück mit Content-Type `audio/mpeg`

#### Warum als Bridge im Backend statt direkt im Frontend?

Wäre das Frontend direkt mit ElevenLabs verbunden, müsste der ElevenLabs-API-Key im Browser sichtbar sein. Über die Backend-Bridge bleibt der Key sicher auf Railway.

---

### 6.6 Erweiterung 5: Multi-User-Tracking

#### WARUM wurde diese Erweiterung gebaut?

PROMISE war ursprünglich für Single-User-Szenarien gedacht. Alle Agents liegen in der gleichen Datenbank ohne Zuordnung zu einem konkreten User. Oblivio braucht aber Multi-User-Support: Jeder User soll nur seine eigenen Biographer-Sessions sehen.

#### WAS wurde konkret hinzugefügt?

**(a) Neue Spalte `userId` in der Agent-Klasse**

In [`model/Agent.java`](../src/main/java/ch/zhaw/statefulconversation/model/Agent.java) wurde ergänzt:
```java
// User-ID für Multi-User-Tracking
private String userId;

public String getUserId() { return this.userId; }
public void setUserId(String userId) { this.userId = userId; }
```

**(b) Setzen der userId in AgentMetaUtility**

```java
if (data.getUserId() != null && !data.getUserId().isBlank()) {
    result.setUserId(data.getUserId());
}
```

**(c) UserLogController (komplett neue Klasse)**

[`UserLogController.java`](../src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java) mit drei Endpoints:

| Endpoint | Zweck |
|---|---|
| `GET /user/{userId}/agents` | Liste aller Agents eines Users |
| `GET /user/{userId}/conversations` | Alle Gespräche eines Users |
| `GET /user/{userId}/stats` | Statistik |

---

### 6.7 Erweiterung 6: Logging-Pipeline mit Server-Sent Events

#### WARUM wurde diese Erweiterung gebaut?

Beim Debugging eines komplexen State-Machine-Verhaltens war es nötig, **Live-Logs im Browser** zu sehen — ohne SSH-Zugriff auf Railway.

#### WAS macht das System genau?

**Vier neue Klassen** im neuen Paket `logging/`:

- [`LogEvent.java`](../src/main/java/ch/zhaw/statefulconversation/logging/LogEvent.java) — DTO mit timestamp, level, logger, message
- [`SseLogAppender.java`](../src/main/java/ch/zhaw/statefulconversation/logging/SseLogAppender.java) — Custom Logback-Appender, registriert in `logback-spring.xml`
- [`LogStreamBroadcaster.java`](../src/main/java/ch/zhaw/statefulconversation/logging/LogStreamBroadcaster.java) — Hält Liste aller SSE-Subscriber
- [`LogStreamController.java`](../src/main/java/ch/zhaw/statefulconversation/logging/LogStreamController.java) — REST-Endpoint `GET /logs/stream`

Im Browser:
```javascript
const evt = new EventSource('https://promise-production.up.railway.app/logs/stream');
evt.onmessage = (e) => console.log(e.data);
```

---

### 6.8 Erweiterung 7: Biographer-Endpoint im AgentMetaController

#### WARUM wurde diese Erweiterung gebaut?

PROMISE liefert nur `POST /agent/singlestate` für einfache Agents. Für den Biographer brauchte es einen eigenen Endpoint.

#### WAS wurde geändert?

In [`AgentMetaController.java`](../src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) wurde ergänzt:

```java
@PostMapping("agent/biographer")
public ResponseEntity<AgentInfoView> createBiographer(@RequestBody BiographerAgentCreateDTO data) {
    if (data == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    if (AgentMetaType.biographer.getValue() != data.getType()) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Agent agent = AgentMetaUtility.createBiographerAgent(data);
    this.repository.save(agent);
    var result = new AgentInfoView(agent.getId(), agent.getName(),
                                   agent.getDescription(), agent.isActive());
    return new ResponseEntity<>(result, HttpStatus.OK);
}
```

Dazu wurde der `AgentMetaType`-Enum um den Wert `biographer = 1` erweitert.

---

### 6.9 Erweiterung 8: CORS-Konfiguration

#### WARUM wurde diese Erweiterung gebaut?

Das PROMISE-Backend läuft auf Railway, das Frontend auf Hostpoint. Browser blockieren standardmässig solche Cross-Origin-Requests. PROMISE hatte keine CORS-Konfiguration.

#### WAS macht die Konfiguration genau?

[`WebConfig.java`](../src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java):

```java
@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
```

`allowedOriginPatterns("*")` ist für Produktion zu weit gefasst — sollte auf die exakte Frontend-Domain eingeschränkt werden.

---

### 6.10 Zusammenfassung: Was musste man am PROMISE-Code wissen?

Wenn jemand Oblivio nachbauen will, muss er beim PROMISE-Framework folgende Dinge gut verstehen:

1. **Wie `State.respond()` funktioniert** — denn dort wurde `compactIfNeeded()` eingefügt
2. **Wie die State-Kette aufgebaut wird** — denn Oblivio baut sie rückwärts in `AgentMetaUtility`
3. **Wie `StaticExtractionAction` Daten extrahiert** — denn das ist der Mechanismus für Block-Summaries
4. **Wie `TransferUtterancesAction` funktioniert** — denn das übergibt Gesprächsverlauf von Conv → Confirm
5. **Wie `LMOpenAI.decide()` boolean parst** — denn das ist der Mechanismus für Guards
6. **Wie Hibernate-Inheritance via `@DiscriminatorColumn` funktioniert** — denn alle Prompt-Subklassen liegen in einer einzigen Tabelle

---

## 7. Komponenten auf Railway (Java-Backend)

### 7.1 Was läuft hier
Die komplette **Spring Boot Anwendung** mit PROMISE-Framework. Railway hostet einen Docker-Container, der bei jedem `git push origin main` automatisch neu gebaut wird.

### 7.2 Wichtigste Klassen pro Verantwortlichkeit

**REST-API-Endpoints**:
- `controllers/AgentController.java` — Runtime-Endpoints
- `controllers/AgentMetaController.java` — Erzeugungs-Endpoints
- `controllers/TTSController.java` — TTS-Bridge
- `controllers/UserLogController.java` — Multi-User-Endpoints

**State-Machine-Kern**:
- `model/State.java` — Conversation State (mit Compaction-Trigger)
- `model/Transition.java` — Verbindung mit Guards + Actions
- `model/Agent.java` — Top-Level (mit userId)
- `model/Utterances.java` — Gesprächshistorie + Compaction

**Agent-Erzeugung**:
- `controllers/AgentMetaUtility.java` — Factory + 70 Block-Prompts

**OpenAI-Anbindung**:
- `spi/LMOpenAI.java` — API-Calls
- `spi/OpenAIProperties.java` — Singleton mit URL, Key, Model

### 7.3 Endpoints

```
GET    /actuator/health
GET    /agent
POST   /agent/singlestate
POST   /agent/biographer
GET    /{agentId}/info
GET    /{agentId}/conversation
GET    /{agentId}/state
GET    /{agentId}/states
GET    /{agentId}/storage
POST   /{agentId}/start
POST   /{agentId}/respond
POST   /{agentId}/rerespond
DELETE /{agentId}/reset
GET    /{agentId}/summarise
POST   /{agentId}/tts?voice_id=...
GET    /user/{userId}/agents
GET    /user/{userId}/conversations
GET    /user/{userId}/stats
GET    /logs/stream
```

### 7.4 Environment-Variablen auf Railway
```
SPRING_DATASOURCE_URL=jdbc:postgresql://<project>.supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=...
OPENAI_KEY=sk-proj-...
ELEVENLABS_API_KEY=...
SPRING_PROFILES_ACTIVE=prod
```

### 7.5 Build-Prozess
Multi-Stage Docker Build:
1. Build-Stage: JDK + Maven Wrapper → JAR
2. Runtime-Stage: JRE + JAR
3. Healthcheck auf `/actuator/health`

Auto-Deploy bei `git push origin main`.

---

## 8. Komponenten auf Hostpoint (Frontend)

### 8.1 Was läuft hier
Reine **statische Dateien** ohne Build-Pipeline. Manueller Upload via FTP/SFTP.

### 8.2 Verzeichnisstruktur

```
Website/
├── index.html                    Landing
├── biographer.html               Biographer-UI mit Pre-Survey + Chat
├── legacy.html                   Legacy-Chat mit 3 Varianten
├── journey.html                  User-Dashboard
├── about.html, faq.html, ...
├── signup.html, login.html
├── 404.html
├── audio/background.mp3
├── images/
│   ├── logo.png
│   └── avatars/*.jpg (pro Persona)
└── js/
    ├── config.js                 Supabase URL + Backend URL
    ├── translations.js           i18n-Engine
    ├── lang-de.js ... lang-zh.js 8 Sprachen
    ├── biographer-promise.js     API-Client für Biographer
    └── legacy-chat.js            API-Client für Legacy
```

### 8.3 Verantwortlichkeiten pro Datei

**`Website/legacy.html`** (~2000 Zeilen): Zugangscode-Eingabe, Visitor-Info, Mode-Toggle, Chat-UI, Voice-Wiedergabe, localStorage-Persistierung.

**`Website/biographer.html`** (~3000 Zeilen): Pre-Survey (Block 0), Sprachauswahl, Chat-UI, Fortschrittsbalken, Abschluss-Karte mit Zugangscode.

**`Website/js/legacy-chat.js`** (~300 Zeilen): `buildVisitorContext()`, `buildLegacySystemPrompt()`, `createLegacyAgent()`, `startLegacyConversation()`, `sendLegacyMessage()`.

**`Website/js/translations.js`**: i18n-Engine, lädt `lang-<code>.js` dynamisch, ersetzt `[data-i18n="key"]` Attribute.

### 8.4 Verwendete Libraries (alle CDN)
- **Supabase JS Client** v2
- **Google Fonts**: Crimson Pro + DM Serif Display
- Vanilla JS, kein Framework

### 8.5 Upload nach Hostpoint
Via FTP-Client (FileZilla, Cyberduck). Häufig geänderte Dateien:
- `legacy.html`, `js/lang-*.js`, `images/avatars/*.jpg`

---

## 9. Komponenten auf Supabase (DB + Auth)

### 9.1 Was läuft hier
- **PostgreSQL-Datenbank**
- **Authentication** (JWT)
- **Row-Level Security**

Die DB wird von **zwei Seiten** beschrieben:
1. **Backend (Railway)** via JDBC + Hibernate
2. **Frontend (Hostpoint)** via Supabase JS Client

### 9.2 Tabellen — wer schreibt was

| Tabelle | Wer schreibt | Wer liest | RLS |
|---|---|---|:-:|
| `auth.users` | Supabase Auth | Frontend (mit JWT) | ✓ |
| `user_agents` | Frontend | Frontend (mit JWT) | ✓ |
| `user_legacies` | Frontend | Frontend (mit JWT) | ✓ |
| `legacy_access_codes` | Manuell | Frontend (Anon-Key) | partiell |
| `legacy_messages` | Frontend | Frontend (filter visitor_id) | partiell |
| `questionnaire_answers` | Frontend | Frontend (mit JWT) | ✓ |
| `agent` (PROMISE) | Backend via JPA | Backend | — |
| `state`, `utterance`, ... | Backend via JPA | Backend | — |

### 9.3 Backend-Konfiguration

In `src/main/resources/application-prod.properties`:
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.data-source-properties.prepareThreshold=0
```

---

## 10. Externe APIs (OpenAI + ElevenLabs)

### 10.1 OpenAI

**Endpoint**: `https://api.openai.com/v1/chat/completions`
**Modell**: GPT-4o
**Wer ruft auf**: Nur das Backend.

**Wofür**: Completion (Antworten), Decide (Guards), Extract (JSON-Daten), Summarise.

**Kosten**: ~$5–15 pro 1 Mio Tokens. Biographer-Session: ~$0.50. Legacy-Chat: ~$0.10.

### 10.2 ElevenLabs

**Endpoint**: `https://api.elevenlabs.io/v1/text-to-speech/{voice_id}`
**Modell**: `eleven_multilingual_v2`
**Voice-IDs**: in `legacy_access_codes.voice_id`.
**Kosten**: Free 10k Zeichen/Monat, Bezahlplan ab ~$5/Monat.

---

## 11. Der Biographer im Detail

### 11.1 21 States — Aufbau

```
Block 1 Conv ──Guard──► Block 1 Confirm ──Guard──► Block 2 Conv
   │                       │
   │ TransferUtterances    │ StaticExtraction → Storage["block1"]
   ▼                       ▼
```

### 11.2 7 Prompt-Komponenten pro Block

| Index | Komponente |
|:-:|---|
| 0 | Conv System-Prompt |
| 1 | Conv Starter-Prompt |
| 2 | Conv Guard |
| 3 | Confirm System-Prompt |
| 4 | Confirm Starter |
| 5 | Confirm Guard |
| 6 | Extract-Prompt |

### 11.3 Block-Themen + Fragen-Anzahl

| Block | Thema | Fragen |
|:-:|---|:-:|
| 1 | Geschmack & Vorlieben | 11 |
| 2 | Alltag & Lebenswelt | 5 |
| 3 | Kommunikationsstil | 5 |
| 4 | Erinnerungen | 4 |
| 5 | Emotionen & Beziehungen | 4 |
| 6 | Beziehungen & Fremdbild | 4 |
| 7 | Werte & Überzeugungen | 5 |
| 8 | Macken & Widersprüche | 5 |
| 9 | Vermächtnis & Zukunft | 5 |
| 10 | Abschluss | 3 |

### 11.4 Doppelte Qualitätskontrolle

- **Guard-Decision**: prüft Vollständigkeit der Fragen (binär)
- **System-Prompt-Tiefe**: bei oberflächlichen Antworten formuliert das LLM selbst eine Rückfrage

### 11.5 Was passiert beim Übergang Conv → Confirm

1. User-Nachricht kommt rein
2. `State.respond()` → `acknowledge()` → fügt Nachricht hinzu
3. `compactIfNeeded()` wird aufgerufen
4. `raiseIfTransit()` prüft Transitions
5. `Transition.decide()` ruft Guard auf
6. Falls true: `TransferUtterancesAction` kopiert Utterances
7. `TransitionException` → currentState = Confirm
8. Confirm-State antwortet mit Zusammenfassung

### 11.6 Was passiert beim Übergang Confirm → Nächster Block

1. User bestätigt
2. Confirm-Guard → true
3. `StaticExtractionAction` → JSON extrahieren → `storage["blockN"]`
4. Transition zum nächsten Block-Conv-State
5. Utterances werden **NICHT** übernommen (jeder Block startet frisch)

---

## 12. Der Legacy-Chat im Detail

### 12.1 Single-State-Architektur

```
LegacyState ──Guard("verabschiedet sich?")──► Final
   │
   │ StaticExtractionAction("summary")
   ▼
```

### 12.2 Wer baut den Prompt?

**Das Frontend** ([`legacy-chat.js:buildLegacySystemPrompt()`](../Website/js/legacy-chat.js#L98)):
1. Lädt `legacy_data` aus Supabase
2. Wählt je nach Modus den richtigen Prompt
3. Hängt Visitor-Context-Block an
4. Sendet als `statePrompt` an `POST /agent/singlestate`

### 12.3 Starter-Prompt-Logik

```javascript
if (mode === 'passive' || mode === 'analysis') {
    starterPrompt = "Antworte mit GENAU: __WAIT__";
} else {
    // active: persona begrüsst
    starterPrompt = `Begrüsse warmherzig als ${nickname}...`;
}
```

### 12.4 Mode-Switching

Bei Variantenwechsel: neuer PROMISE-Agent mit anderem Prompt, bisherige Nachrichten als Kontext, Visitor-ID-Wechsel.

---

## 13. Mehrsprachigkeit

### 13.1 Frontend-i18n

**Engine**: `Website/js/translations.js`. **Dateien**: `lang-<code>.js` für 8 Sprachen.

```javascript
window.OBLIVIO_TRANSLATIONS['de'] = {
    nav_home: 'Startseite',
    // ... ~400 Keys
};
```

HTML: `<button data-i18n="legacy_send">Senden</button>`. Aktive Sprache in `localStorage('oblivio_language')`.

### 13.2 Backend-Sprachsteuerung

In `AgentMetaUtility.getLanguageInstruction()`:
```java
switch (language) {
    case "en": return "WICHTIG: ... auf Englisch kommunizieren...";
    case "ko": return "... auf Koreanisch ...";
    // 8 Sprachen
}
```

**Trick**: Block-Prompts liegen **nur auf Deutsch** vor. GPT-4o übersetzt zur Laufzeit. Spart Speicheraufwand (700 statt 5600 Strings).

---

## 14. Visitor-Tracking

### 14.1 Browser-Visitor-ID

```javascript
let id = localStorage.getItem('oblivio_visitor_id');
if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem('oblivio_visitor_id', id);
}
```

### 14.2 Mode-Scoping

```javascript
function getScopedVisitorId(mode) {
    return baseVisitorId + '__' + mode;
}
// z.B. "abc-123__active"
```

### 14.3 Visitor-Info pro Persona

```javascript
localStorage.setItem('oblivio_visitor_' + code, JSON.stringify({
    name: 'Maria',
    relation: 'child',
    gender: 'female'
}));
```

---

## 15. Persona-Prompt-Architektur

### 15.1 6 Sektionen pro Persona

```
[SECTION:IDENTITY]      ← Wer bin ich, Sprache, Eröffnungsverhalten
[SECTION:CHAPTERS]      ← 10 Lebenskapitel
[SECTION:ANALYSIS]      ← Persönlichkeitsanalyse (nur Variante 1)
[SECTION:STYLE]         ← Schreibstil, Wortwahl
[SECTION:EXAMPLES]      ← Dialog-Beispiele
[SECTION:SELF_KNOWLEDGE] ← Ich-Form-Selbstwissen
[SECTION:RULES]         ← 10 Verhaltensregeln
```

### 15.2 3 Varianten

| Variante | UI-Label | Sektionen | Verhalten |
|:-:|---|---|---|
| 1 | "Variante 1" | + ANALYSIS | Wartet, mit Analyse |
| 2 | "Variante 2" | IDENTITY aktiv | Persona begrüsst |
| 3 | "Variante 3" | ohne ANALYSIS | Wartet |

### 15.3 Speicherung

`legacy_access_codes.legacy_data` (JSONB):
```json
{
  "full_prompt_active": "...",
  "full_prompt_passive": "...",
  "full_prompt_analysis": "...",
  "block1": "...", "block2": "...", ...
}
```

### 15.4 `__WAIT__`-Token-Mechanismus

Im Passiv- und Analyse-Modus erhält der Agent als Starter-Prompt:
> *"Antworte mit GENAU diesem Text und nichts anderem: __WAIT__"*

Das LLM gibt `__WAIT__` zurück. Das Frontend filtert es heraus.

---

## 16. TTS-Integration

[`TTSController.java`](../src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java):

```java
@PostMapping("{agentID}/tts")
public ResponseEntity<byte[]> textToSpeech(...) {
    String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId + "?output_format=mp3_44100_128";
    // ElevenLabs-Request
    return ResponseEntity.ok(audioBytes);
}
```

Frontend:
```javascript
const response = await fetch(`${promiseUrl}/${agentId}/tts?voice_id=${voiceId}`, {
    method: 'POST',
    body: JSON.stringify({ text: assistantText })
});
new Audio(URL.createObjectURL(await response.blob())).play();
```

---

## 17. Datenbank-Schema

### 17.1 Oblivio-spezifische Tabellen

```sql
CREATE TABLE user_agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    agent_id UUID,
    language TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE user_legacies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    legacy_data JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE legacy_access_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    access_code TEXT UNIQUE,
    nickname TEXT,
    language TEXT,
    is_active BOOLEAN DEFAULT true,
    legacy_data JSONB,
    avatar_url TEXT,
    voice_id TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE legacy_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    access_code TEXT REFERENCES legacy_access_codes(access_code),
    visitor_id TEXT,
    visitor_name TEXT,
    user_id UUID DEFAULT auth.uid(),
    role TEXT,
    content TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
```

### 17.2 PROMISE-eigene Tabellen (von Hibernate automatisch erzeugt)

- `agent`, `prompt` (mit `dtype`-Discriminator), `state`, `decision`, `action`
- `transition`, `prompt_transitions`
- `utterance`, `utterances`
- `storage`, `storage_entry`

---

## 18. Schritt-für-Schritt: Oblivio von Null nachbauen

### Phase 1: Vorbereitung
- GitHub, Railway, Supabase, Hostpoint, OpenAI, ElevenLabs Accounts
- Java 21, Maven Wrapper, Git, SQL-Client
- PROMISE forken: `git clone https://github.com/2024-ZHAW-PM4/promise.git`

### Phase 2: Supabase einrichten
1. Projekt erstellen, Connection-String holen
2. Tabellen anlegen: `psql <conn> -f sql/SUPABASE_TABLES.sql`
3. RLS-Policies anlegen

### Phase 3: Backend lokal konfigurieren
1. `application.properties` aus Template erstellen
2. `./mvnw spring-boot:run`
3. Health-Check: `curl http://localhost:8080/actuator/health`

### Phase 4: AgentMetaUtility erweitern
1. `createBiographerAgent()` implementieren
2. `buildBlockPrompts()` mit allen 70 Prompts
3. `getLanguageInstruction()` für gewünschte Sprachen
4. `/agent/biographer`-Endpoint im Controller

### Phase 5: Context Compaction einbauen
1. `Utterances.compactIfNeeded()` implementieren
2. In `State.respond()` aufrufen
3. `LMOpenAI.summariseOffline()` hinzufügen

### Phase 6: TEXT-Spalten umstellen
- `Prompt.java`, `State.java`, `Utterance.java`: `@Column(columnDefinition = "TEXT")`

### Phase 7: TTS-Controller hinzufügen
- Neue Datei `TTSController.java` mit ElevenLabs-Bridge

### Phase 8: Backend deployen auf Railway
- Dockerfile, `railway.json`, Env-Variablen, `git push`

### Phase 9: Frontend aufbauen
- HTML-Seiten, Supabase JS Client, `config.js`, `translations.js`, `legacy-chat.js`

### Phase 10: i18n erweitern
- Pro Sprache: `lang-<code>.js` übersetzen + `getLanguageInstruction()`-Case ergänzen

### Phase 11: Auth verbinden
- Supabase-Auth in allen HTML-Seiten
- `user_agents`-Eintrag nach Biographer-Start

### Phase 12: Hostpoint-Upload
- FTP-Client, Website/ hochladen, Domain konfigurieren, HTTPS

### Phase 13: Personas anlegen
- Biographer-Durchlauf → Block-Summaries → 3 Variante-Prompts → SQL-Insert
- Avatar hochladen, Voice trainieren

### Phase 14: Testen
- End-to-End-Tests aller Flows

---

## 19. Datei-Index: Was steht wo

### Backend (Java / Railway)

```
src/main/java/ch/zhaw/statefulconversation/
├── StatefulconversationApplication.java   Spring Boot Entry Point
├── config/
│   └── WebConfig.java                     ★ CORS (neu in Oblivio)
├── controllers/
│   ├── AgentController.java               PROMISE Original
│   ├── AgentMetaController.java           ★ + /agent/biographer
│   ├── AgentMetaUtility.java              ★ Biographer-Factory + 70 Block-Prompts
│   ├── AgentMetaType.java                 ★ + biographer=1
│   ├── TTSController.java                 ★ Neu in Oblivio
│   ├── UserLogController.java             ★ Neu in Oblivio
│   ├── dto/
│   │   ├── BiographerAgentCreateDTO.java  ★ Neu
│   │   └── SingleStateAgentCreateDTO.java PROMISE Original
│   └── views/
│       ├── TTSRequest.java                ★ Neu
│       ├── UserAgentView.java             ★ Neu
│       └── UserConversationView.java      ★ Neu
├── logging/                               ★ Komplettes Paket neu
│   ├── LogEvent.java
│   ├── LogStreamBroadcaster.java
│   ├── LogStreamController.java
│   └── SseLogAppender.java
├── model/
│   ├── Agent.java                         ★ + userId
│   ├── State.java                         ★ + compactIfNeeded() Aufruf + TEXT
│   ├── Prompt.java                        ★ + TEXT
│   ├── Utterance.java                     ★ + TEXT
│   ├── Utterances.java                    ★ + compactIfNeeded() Methode
│   ├── Final.java, Transition.java        PROMISE Original
│   ├── Decision.java, Action.java         PROMISE Original
│   ├── Storage.java, StorageEntry.java    PROMISE Original
│   ├── Response.java, PromptResult.java   PROMISE Original
│   ├── TransitionException.java           PROMISE Original
│   ├── OuterState.java                    PROMISE Original
│   └── commons/                           PROMISE Original
│       ├── actions/, decisions/, states/
├── repositories/                          PROMISE Original
└── spi/
    ├── LMOpenAI.java                      ★ + summariseOffline()
    ├── OpenAIProperties.java              PROMISE Original
    ├── ContenFilterException.java         PROMISE Original
    └── GsonExclude.java                   PROMISE Original
```

★ = Oblivio-spezifische Änderungen oder Erweiterungen.

### Frontend (HTML / Hostpoint)

```
Website/
├── index.html                  ★ Landing
├── biographer.html             ★ Biographer-UI
├── legacy.html                 ★ Legacy-Chat mit 3 Varianten
├── journey.html                ★ Dashboard
├── signup.html, login.html     ★ Auth
├── about.html, faq.html, ...   ★ alle neu
├── images/avatars/*.jpg        ★ Pro Persona
└── js/
    ├── config.js               ★ URLs
    ├── translations.js         ★ i18n
    ├── lang-*.js (8 Sprachen)  ★
    ├── biographer-promise.js   ★
    └── legacy-chat.js          ★
```

### Datenbank-Skripte

```
sql/
├── SUPABASE_TABLES.sql         Initiale Struktur
└── supabase_migrations.sql     Spätere Erweiterungen
```

### Konfiguration & Build

```
.gitignore, .railwayignore, .env.example
.mvn/wrapper/
Dockerfile
railway.json
pom.xml
mvnw, mvnw.cmd
```

---

## 20. Bekannte Limitationen

### 20.1 Token-Kosten
GPT-4o ist nicht günstig. Mitigation: Context Compaction.

### 20.2 Hibernate `ddl-auto=update`
Erweitert Tabellen, ändert nicht Spaltentypen. Manuelle Migration nötig.

### 20.3 Railway Cold Start
5–30 Sekunden bei Inaktivität. Mitigation: 3-Versuche-Retry im Frontend.

### 20.4 ElevenLabs Rate Limits
Free Tier: 10k Zeichen/Monat.

### 20.5 Supabase Anon-Key sichtbar
`legacy_access_codes` muss lesbar sein. Daten technisch öffentlich abrufbar.

### 20.6 PROMISE Cascading Bug
Bei kombinierten Trigger+Guard kann es zu Kaskaden kommen. Workaround: Nur Guards im Biographer.

### 20.7 Block-Prompts hardcoded
70 Strings in `AgentMetaUtility.java`. Auslagerung in JSON wäre besser.

### 20.8 Keine Tests
Keine Unit-/Integration-Tests im Repo.

### 20.9 Frontend ohne Build-System
Vanilla JS. Bei Wachstum auf Vite/Webpack migrieren.

---

## Anhang A: Persona-SQL-Template

```sql
UPDATE legacy_access_codes
SET legacy_data = COALESCE(legacy_data, '{}'::jsonb)
  || jsonb_build_object(
    'full_prompt_active',   $$<aktiver Prompt>$$,
    'full_prompt_passive',  $$<passiver Prompt>$$,
    'full_prompt_analysis', $$<analyse Prompt>$$
  ),
  nickname   = '<Name>',
  voice_id   = '<ElevenLabs Voice ID>',
  avatar_url = 'images/avatars/<name>.jpg'
WHERE access_code = '<8-stelliger Code>';
```

## Anhang B: API-Cheatsheet

```bash
# Biographer starten
curl -X POST https://promise-production.up.railway.app/agent/biographer \
  -H "Content-Type: application/json" \
  -d '{"type":1,"agentName":"Bio","language":"de","nickname":"Anna"}'

# Nachricht
curl -X POST https://promise-production.up.railway.app/<id>/respond \
  -H "Content-Type: application/json" \
  -d '{"content":"Hallo"}'

# Current State
curl https://promise-production.up.railway.app/<id>/state

# Storage (Block-Summaries)
curl https://promise-production.up.railway.app/<id>/storage

# Reset
curl -X DELETE https://promise-production.up.railway.app/<id>/reset

# TTS
curl -X POST "https://promise-production.up.railway.app/<id>/tts?voice_id=<voiceId>" \
  -H "Content-Type: application/json" \
  -d '{"text":"Hallo"}' --output output.mp3
```

---

**Autor:** Dennis Riccardo
**Institution:** ZHAW School of Management and Law, Business Informatics
**Bachelorarbeit:** 2026
**Repository:** https://github.com/riccaden/promise
**Lizenz:** Academic
