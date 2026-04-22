<div align="center">

# Oblivio

**Beyond Memory — A Digital Legacy Platform**

*Preserve your essence. Share your wisdom. Leave more than just memories.*

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o-412991?logo=openai&logoColor=white)](https://openai.com/)
[![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)
[![Railway](https://img.shields.io/badge/Railway-Deployed-0B0D0E?logo=railway&logoColor=white)](https://railway.app/)
[![ElevenLabs](https://img.shields.io/badge/ElevenLabs-TTS-000000?logo=data:image/svg+xml;base64,PHN2Zy8+&logoColor=white)](https://elevenlabs.io/)

[![Personas](https://img.shields.io/badge/Personas-11-orange)](#personas)
[![Languages](https://img.shields.io/badge/Languages-8-blue)](#languages)
[![Variants](https://img.shields.io/badge/Conversation_Variants-3-green)](#legacy-agent--digital-persona)
[![License](https://img.shields.io/badge/License-Academic-lightgrey)](#author)

---

*Bachelor's Thesis · ZHAW School of Management and Law · Business Informatics · 2026*

</div>

---

## Why Oblivio?

Everyone has a story worth preserving. But traditional methods — written biographies, video recordings, photo albums — capture *content* without capturing *personality*. They are static. You can read someone's words, but you can't have a new conversation with them.

Oblivio changes this. It uses conversational AI to not only **record** a life story, but to **recreate the way someone speaks, thinks, and feels** — so that loved ones can continue talking to that person's digital essence long after the original conversation ends.

> *"Not a form. Not a questionnaire. A conversation that actually listens."*

---

## What is Oblivio?

Oblivio is a web application for digitally preserving life stories. People tell their story through a guided AI interview (**Biographer Agent**) — from which an interactive digital persona (**Legacy Agent**) is created that loved ones can talk to in the future.

Built as a bachelor's thesis at [ZHAW](https://www.zhaw.ch), powered by the [PROMISE Framework](https://github.com/2024-ZHAW-PM4) for state-based conversational AI.

---

## Concept

### Biographer Agent — Capturing Life Stories

A guided interview across **10 thematic blocks** that systematically captures a person's personality, memories, and values:

| Block | Theme | Purpose |
|:-----:|-------|---------|
| 1 | Tastes & Preferences | Ice-breaker, personal flavour |
| 2 | Daily Life & World | Routines, home, work |
| 3 | Communication Style | How they speak and write |
| 4 | Key Memories | Defining moments |
| 5 | Emotions & Relationships | Patterns, love, conflict |
| 6 | Relationships & Perception | How others see them |
| 7 | Values & Change | Beliefs, growth |
| 8 | Quirks & Contradictions | Hidden sides |
| 9 | Legacy & Future | What to leave behind |
| 10 | Self-Reflection | Final portrait |

Each block consists of a **conversation phase** and a **confirmation phase**. At the end of each block, a summary is extracted and stored — forming the foundation for the Legacy Agent.

### Legacy Agent — Digital Persona

From the collected summaries, a system prompt is generated that creates an **interactive AI persona**. Loved ones can speak with this persona — it responds in the style, with the values, and with the memories of the captured person.

**Three conversation variants:**

| Variant | Name | Description |
|:-------:|------|-------------|
| 1 | **Active** | Persona greets first and leads the conversation |
| 2 | **Passive** | Persona waits silently, responds only when spoken to |
| 3 | **Analysis** | Includes personality analysis (radar chart, communication DNA, life pattern) |

### Prompt Architecture

Each persona prompt is structured in clearly separated sections:

```
[SECTION:IDENTITY]        Who the person is — core traits, background, age
[SECTION:CHAPTERS]        10 thematic summaries from the Biographer interview
[SECTION:ANALYSIS]        Personality radar, communication DNA, life pattern (Variant 3 only)
[SECTION:STYLE]           Language patterns, dialect, vocabulary, sentence structure
[SECTION:SELF_KNOWLEDGE]  What the persona knows about itself and the platform
[SECTION:RULES]           Behavioural constraints — no lists, no AI phrases, stay in character
```

---

## Personas

Oblivio currently hosts **11 personas** across **4 native languages**, each available in **3 conversation variants**:

| # | Language | Age | Profile |
|:-:|:--------:|:---:|---------|
| 1 | Deutsch | 56 | Father, self-employed, pragmatic humour |
| 2 | Italiano | 81 | Grandmother, Neapolitan dialect, warm storyteller |
| 3 | Italiano | 62 | Father, Sicilian roots, direct and emotional |
| 4 | Italiano | 26 | Cousin, Gen-Z, ironic and pop-culture savvy |
| 5 | 日本語 | 28 | Friend, Tokyo dialect, reflective and poetic |
| 6 | Deutsch | 28 | Close friend, Swiss-German patterns, analytical |
| 7 | 日本語 | 72 | Grandmother, Kansai dialect, traditional wisdom |
| 8 | 한국어 | 24 | Friend, Seoul dialect, warm and emotionally open |
| 9 | Deutsch | 63 | Father, Austrian roots, structured and principled |
| 10 | Deutsch | 27 | Best friend, Swiss-German, spontaneous and loyal |
| 11 | Italiano | 30 | Cousin, Roman dialect, streetwise and genuine |

Each persona includes a **custom voice** via ElevenLabs TTS and a **profile photo** displayed in the chat interface.

---

## Languages

Oblivio supports **8 languages** for the Biographer interview and UI:

| Language | Code | Biographer | Legacy Chat | UI |
|----------|:----:|:----------:|:-----------:|:--:|
| Deutsch | `de` | ✓ | ✓ | ✓ |
| English | `en` | ✓ | ✓ | ✓ |
| Français | `fr` | ✓ | ✓ | ✓ |
| Italiano | `it` | ✓ | ✓ | ✓ |
| Türkçe | `tr` | ✓ | ✓ | ✓ |
| 한국어 | `ko` | ✓ | ✓ | ✓ |
| 日本語 | `ja` | ✓ | ✓ | ✓ |
| 中文 | `zh` | ✓ | ✓ | ✓ |

> **Note:** The Biographer can conduct interviews in all 8 languages. Legacy personas respond in their native language but understand messages in any supported language.

---

## Key Features

- **Context Compaction** — After 20 user messages, older conversation history is automatically summarised and compressed, reducing token usage while preserving key context
- **Per-Persona Voice** — Each persona has a custom ElevenLabs voice for text-to-speech synthesis
- **Visitor Isolation** — Each browser session gets a unique visitor ID; conversations are private per device and per mode
- **Conversation Persistence** — Messages are stored in the database and restored on return visits
- **Anti-AI Patterns** — Prompts include explicit rules against lists, structured responses, assistant phrases, and repetitive greetings
- **Multi-Variant Access** — Each persona can be experienced in 3 different conversation styles via a single access code

---

## Architecture

```
┌─────────────┐     HTTPS      ┌──────────────────────────────┐
│   Browser    │ ──────────────→│   Spring Boot REST API       │
│  (HTML/JS)   │                │        (Railway)             │
└─────────────┘                │                              │
                               │  ┌────────────────────────┐  │
                               │  │  PROMISE State Machine  │  │
                               │  │  ┌──────┐  ┌────────┐  │  │
                               │  │  │States│→ │Transitions│ │  │
                               │  │  └──────┘  └────────┘  │  │
                               │  └────────────┬───────────┘  │
                               │               │              │
                               └───────────────┼──────────────┘
                                               │
                          ┌────────────────────┼────────────────────┐
                          │                    │                    │
                          ▼                    ▼                    ▼
                   ┌─────────────┐    ┌──────────────┐    ┌──────────────┐
                   │   OpenAI    │    │   Supabase   │    │  ElevenLabs  │
                   │   GPT-4o   │    │  PostgreSQL  │    │     TTS      │
                   └─────────────┘    └──────────────┘    └──────────────┘
```

---

## Tech Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Backend** | Java 21, Spring Boot, Maven | REST API, PROMISE state machine |
| **Database** | PostgreSQL (Supabase) | Authentication, storage, RLS |
| **Frontend** | HTML5, Vanilla JS | No framework dependency |
| **AI Engine** | OpenAI GPT-4o (via PROMISE) | Conversations, summaries, analysis |
| **TTS** | ElevenLabs API | Voice synthesis for each persona |
| **Hosting** | Railway (Docker) | Auto-deploy on push to `main` |
| **Web** | Swiss Hosting Provider | Frontend delivery |
| **Auth** | Supabase Authentication | JWT-based, bcrypt passwords |

---

## Project Structure

```
src/main/java/ch/zhaw/statefulconversation/
├── controllers/              # REST endpoints
│   ├── AgentController.java        # Agent interaction (start, respond, reset)
│   ├── AgentMetaController.java    # Agent creation (biographer, singlestate)
│   ├── AgentMetaUtility.java       # Block prompts, language handling
│   ├── TTSController.java          # Text-to-Speech (ElevenLabs)
│   └── UserLogController.java      # User tracking
├── model/                    # Domain model
│   ├── Agent.java                  # Agent lifecycle
│   ├── State.java                  # Conversation state (extends Prompt)
│   ├── Prompt.java                 # Base prompt entity
│   ├── Transition.java             # State transitions (guards, triggers)
│   ├── Utterances.java             # Message history + context compaction
│   ├── Storage.java                # Key-value storage for summaries
│   └── commons/states/             # Specialised state types
├── repositories/             # JPA repositories
├── spi/                      # OpenAI integration
│   └── LMOpenAI.java              # GPT API calls, prompt composition
└── config/                   # Spring configuration

Website/                      # Static frontend (hosted separately)
├── index.html                # Landing page
├── biographer.html           # Biographer interview UI
├── legacy.html               # Legacy chat UI (3 variants)
├── js/
│   ├── biographer-promise.js # PROMISE API client for biographer
│   ├── legacy-chat.js        # Legacy chat API client
│   ├── translations.js       # i18n core (dynamic loading)
│   └── lang-*.js             # Translation files (8 languages)
└── images/avatars/           # Persona profile photos
```

---

## API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/agent` | List all agents |
| `POST` | `/agent/singlestate` | Create legacy agent |
| `POST` | `/agent/biographer` | Create biographer agent |
| `POST` | `/{agentId}/start` | Start conversation |
| `POST` | `/{agentId}/respond` | Send message |
| `GET` | `/{agentId}/conversation` | Get conversation history |
| `GET` | `/{agentId}/state` | Get current state |
| `GET` | `/{agentId}/storage` | Get extracted summaries |
| `POST` | `/{agentId}/tts?voice_id=...` | Generate TTS audio |
| `DELETE` | `/{agentId}/reset` | Reset conversation |

---

## Local Development

### Prerequisites
- Java 21 (JDK)
- Maven
- PostgreSQL
- OpenAI API Key
- (Optional) ElevenLabs API Key

### Setup

```bash
# 1. Clone
git clone https://github.com/riccaden/promise.git
cd promise

# 2. Create config files
cp src/main/resources/application.properties.template src/main/resources/application.properties
cp src/main/resources/openai.properties.template src/main/resources/openai.properties

# 3. Configure database in application.properties
# spring.datasource.url=jdbc:postgresql://localhost:5432/oblivio

# 4. Set OpenAI key in openai.properties
# openai.key=sk-...

# 5. Run
./mvnw spring-boot:run

# 6. Open http://localhost:8080
```

---

## Deployment

Production runs on **Railway** (backend) with **Supabase** (database + auth).

| Service | Platform | Auto-Deploy |
|---------|----------|:-----------:|
| Java Backend | Railway | ✓ (on push to `main`) |
| Database | Supabase PostgreSQL | — |
| Frontend | Swiss Hosting (Hostpoint) | Manual upload |

Environment variables on Railway:
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
OPENAI_KEY
ELEVENLABS_API_KEY
PORT (auto-set by Railway)
```

---

## Author

**Dennis Riccardo Dewiri**

Bachelor's Thesis · [ZHAW School of Management and Law](https://www.zhaw.ch/en/sml/) · Business Informatics · 2026

---

<div align="center">
<sub>Built with conviction that every person deserves to be heard and remembered.</sub>
</div>
