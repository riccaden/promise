# Oblivio

**Oblivio** ist eine Webanwendung zur digitalen Bewahrung von Lebensgeschichten. Menschen erzählen ihr Leben in einem geführten KI-Interview (Biographer Agent) — daraus entsteht eine interaktive digitale Persona (Legacy Agent), mit der Angehörige auch in Zukunft sprechen können.

Entwickelt als Bachelorarbeit an der ZHAW, basierend auf dem [PROMISE-Framework](https://github.com/2024-ZHAW-PM4) für zustandsbasierte Konversationssteuerung.

---

## Konzept

### 1. Biographer Agent — Lebensgeschichte erfassen
Ein geführtes Interview über **10 thematische Blöcke**, das die Persönlichkeit, Erinnerungen und Werte einer Person systematisch erfasst:

| Block | Thema |
|-------|-------|
| 1 | Vorlieben, Abneigungen, Geschmack |
| 2 | Alltag und persönliche Welt |
| 3 | Sprach- und Schreibstil |
| 4 | Schlüsselerinnerungen und Erfahrungen |
| 5 | Emotionen und Beziehungsdynamiken |
| 6 | Beziehungen und Fremdwahrnehmung |
| 7 | Werte, Überzeugungen, Wandel |
| 8 | Eigenheiten, Widersprüche, verborgene Seiten |
| 9 | Vermächtnis und Zukunft |
| 10 | Gesamtbild und Selbstreflexion |

Jeder Block besteht aus einer **Gesprächs-Phase** und einer **Bestätigungs-Phase**. Am Ende jedes Blocks wird eine Zusammenfassung extrahiert und gespeichert.

### 2. Legacy Agent — Digitale Persona
Aus den gesammelten Zusammenfassungen wird ein System-Prompt generiert, der eine **interaktive KI-Persona** erschafft. Angehörige können mit dieser Persona sprechen — sie antwortet im Stil, mit den Werten und den Erinnerungen der erfassten Person.

---

## Tech Stack

| Komponente | Technologie |
|------------|-------------|
| Backend | Java 21, Spring Boot, Maven |
| Datenbank | PostgreSQL (Supabase) |
| Frontend | HTML5, Vanilla JavaScript |
| KI | OpenAI GPT (via PROMISE-Framework) |
| Text-to-Speech | ElevenLabs API |
| Deployment | Railway (Docker) |
| Auth | Supabase Authentication |

---

## Projektstruktur

```
src/main/java/ch/zhaw/statefulconversation/
├── controllers/          # REST-Endpoints (Agent, TTS, UserLog)
│   ├── AgentController.java        # Interaktion mit bestehenden Agents
│   ├── AgentMetaController.java    # Erstellung neuer Agents
│   ├── TTSController.java          # Text-to-Speech (ElevenLabs)
│   └── UserLogController.java      # User-Tracking
├── model/                # Domänenmodell (Agent, State, Transition, Storage)
├── repositories/         # JPA Repositories
├── spi/                  # OpenAI-Integration, Content-Filtering
└── config/               # Spring-Konfiguration

Website/                  # Statisches Frontend
├── index.html            # Landing Page
├── biographer.html       # Biographer-Interview UI
├── legacy.html           # Legacy-Chat UI
└── js/                   # Frontend-Logik

Personas/                 # Persona-Beispiele (Prompts & Profile)
```

---

## Sprachen

Oblivio unterstützt **7 Sprachen** für das Biographer-Interview:
Deutsch, English, Français, Italiano, 日本語, 中文, 한국어

---

## Lokale Entwicklung

### Voraussetzungen
- Java 21 (JDK)
- Maven
- PostgreSQL oder MySQL
- OpenAI API Key
- (Optional) ElevenLabs API Key für TTS

### Setup

1. Properties-Dateien erstellen:
   ```
   src/main/resources/application.properties
   src/main/resources/openai.properties
   ```

2. Datenbank-Verbindung konfigurieren in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/oblivio
   spring.datasource.username=your_user
   spring.datasource.password=your_password
   ```

3. OpenAI Key setzen in `openai.properties`:
   ```properties
   openai.key=sk-...
   ```

4. Starten:
   ```bash
   ./mvnw spring-boot:run
   ```

5. Öffnen: `http://localhost:8080`

---

## Deployment (Railway + Supabase)

Die Produktionsumgebung läuft auf **Railway** mit **Supabase PostgreSQL**.

Umgebungsvariablen auf Railway:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `OPENAI_KEY`
- `ELEVENLABS_API_KEY` (optional)
- `PORT` (automatisch von Railway)

Deployment erfolgt automatisch bei Push auf `main`.

---

## API-Übersicht

| Methode | Endpoint | Beschreibung |
|---------|----------|-------------|
| GET | `/agent` | Alle Agents auflisten |
| POST | `/agent/singlestate` | Legacy Agent erstellen |
| POST | `/agent/biographer` | Biographer Agent erstellen |
| POST | `/{agentId}/start` | Konversation starten |
| POST | `/{agentId}/respond` | Nachricht senden |
| GET | `/{agentId}/conversation` | Gesprächsverlauf abrufen |
| GET | `/{agentId}/state` | Aktuellen State abrufen |
| GET | `/{agentId}/storage` | Gespeicherte Zusammenfassungen abrufen |
| DELETE | `/{agentId}/reset` | Konversation zurücksetzen |

---

## Architektur

```
Benutzer ──→ Website (HTML/JS) ──→ Spring Boot REST API
                                        │
                                        ├──→ PROMISE State Machine (Agent)
                                        │         │
                                        │         └──→ OpenAI GPT
                                        │
                                        ├──→ Supabase PostgreSQL
                                        │
                                        └──→ ElevenLabs TTS (optional)
```

---

## Autor

**Dennis Riccardo Dewiri**
Bachelorarbeit, ZHAW School of Engineering
