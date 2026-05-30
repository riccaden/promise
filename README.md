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
[![Variants](https://img.shields.io/badge/Conversation_Variants-3-green)](#concept)
[![License](https://img.shields.io/badge/License-Academic-lightgrey)](#author)

---

*Bachelor's Thesis · ZHAW School of Management and Law · Business Informatics · 2026*

</div>

---

## Table of Contents

1. [Why Oblivio?](#why-oblivio)
2. [What is Oblivio?](#what-is-oblivio)
3. [Concept](#concept)
4. [Languages](#languages)
5. [Key Features](#key-features)
6. [Architecture](#architecture)
7. [Tech Stack](#tech-stack)
8. [Repository Walkthrough](#repository-walkthrough) ← **start here if you want to navigate the code**
9. [How GitHub → Railway → Supabase works](#how-github--railway--supabase-works)
10. [What Was Adapted From PROMISE](#what-was-adapted-from-promise)
11. [API Overview](#api-overview)
12. [Local Development](#local-development)
13. [Deployment](#deployment)
14. [Author](#author)

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
| 1 | **Analysis** | Persona waits, with personality analysis embedded |
| 2 | **Active** | Persona greets first and leads the conversation |
| 3 | **Passive** | Persona waits silently, responds only when spoken to |

### Prompt Architecture

Each persona prompt is structured in clearly separated sections:

```
[SECTION:IDENTITY]        Who the person is — core traits, background, age
[SECTION:CHAPTERS]        10 thematic summaries from the Biographer interview
[SECTION:ANALYSIS]        Personality radar, communication DNA, life pattern (Variant 1 only)
[SECTION:STYLE]           Language patterns, dialect, vocabulary, sentence structure
[SECTION:SELF_KNOWLEDGE]  What the persona knows about itself and the platform
[SECTION:RULES]           Behavioural constraints — no lists, no AI phrases, stay in character
```

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
                            ┌──────────────────────────────────────────────┐
                            │           BROWSER (User-Device)              │
                            │  - Vanilla JavaScript                        │
                            │  - localStorage (Visitor-IDs, Mode, Lang)    │
                            └──────────────┬───────────────────────────────┘
                                           │ HTTPS GET/POST
                ┌──────────────────────────┼──────────────────────────┐
                ▼                          ▼                          ▼
       ┌───────────────────┐   ┌───────────────────────┐    ┌───────────────────┐
       │  HOSTPOINT (CH)   │   │  RAILWAY (Container)  │    │  SUPABASE (Cloud) │
       │  oblivio.ch       │   │  Java 21 + Spring     │    │  PostgreSQL       │
       │  HTML/CSS/JS      │   │  PROMISE Framework    │    │  Auth (JWT)       │
       │  manual upload    │   │  auto-deploy on push  │    │  RLS Policies     │
       └───────────────────┘   └───────────┬───────────┘    └───────────────────┘
                                           │ JDBC
                          ┌────────────────┴────────────────┐
                          ▼                                 ▼
                  ┌────────────────┐               ┌────────────────┐
                  │   OpenAI API   │               │ ElevenLabs API │
                  │   GPT-4o       │               │   TTS          │
                  └────────────────┘               └────────────────┘
```

Three physical components: **Hostpoint** (frontend), **Railway** (Java backend), **Supabase** (database). Two external APIs: **OpenAI** (LLM) and **ElevenLabs** (voice).

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

## Repository Walkthrough

This section explains what every file in the repository does, what was adapted, and how it connects to Railway and Supabase.

> **Note:** The **frontend** (`Website/`) is NOT in this repo — it lives on Hostpoint, uploaded via FTP. This repo contains only the **Java backend**.

### Root-Level Files

| File | What It Does | Adapted? |
|---|---|---|
| [`README.md`](README.md) | This file — main documentation | ✓ Rewritten for Oblivio |
| [`pom.xml`](pom.xml) | Maven build config + all dependencies | ✓ PostgreSQL driver added (for Supabase) |
| [`Dockerfile`](Dockerfile) | Multi-stage Docker build for Railway | ✓ New for Oblivio |
| [`railway.json`](railway.json) | Railway deployment config | ✓ New |
| [`.railwayignore`](.railwayignore) | Files Railway ignores during build | ✓ New |
| [`.gitignore`](.gitignore) | Files Git ignores | ✓ Extended with Oblivio-specific entries |
| [`.env.example`](.env.example) | Environment variable template | ✓ New |
| [`CITATION.cff`](CITATION.cff) | Citation metadata for academic work | ✓ New |
| [`LICENSE`](LICENSE) | Academic license | — |
| [`mvnw`](mvnw) / [`mvnw.cmd`](mvnw.cmd) | Maven Wrapper (no local Maven needed) | — |

### Java Backend Structure

```
src/main/java/ch/zhaw/statefulconversation/
├── StatefulconversationApplication.java   ← Spring Boot entry point
├── config/                                ★ New CORS config
├── controllers/                           ★ REST API + Biographer factory
├── logging/                               ★ New SSE log streaming
├── model/                                 ← PROMISE state machine
├── repositories/                          ← JPA repositories
└── spi/                                   ★ OpenAI integration (extended)
```

★ = Oblivio-specific additions or modifications.

### `config/` — Spring Configuration

| File | What It Does | Adapted? |
|---|---|---|
| [`config/WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) | CORS configuration — allows the Hostpoint frontend to call the Railway backend | ✓ **New** in Oblivio |

### `controllers/` — REST API Endpoints

| File | What It Does | Adapted? |
|---|---|---|
| [`controllers/AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) | Runtime endpoints: `/{agentId}/start`, `/respond`, `/reset`, `/conversation`, `/state`, `/storage` | PROMISE original |
| [`controllers/AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) | Creation endpoints: `POST /agent/singlestate`, `POST /agent/biographer` | ✓ `/biographer` endpoint added |
| [`controllers/AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java) | Factory: builds the 21-state Biographer + 70 block prompts in 8 languages | ✓ Heavily extended |
| [`controllers/AgentMetaType.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaType.java) | Enum of agent types | ✓ `biographer = 1` added |
| [`controllers/TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) | Bridge to ElevenLabs API for voice synthesis | ✓ **New** |
| [`controllers/UserLogController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java) | Multi-user tracking endpoints: `/user/{id}/agents`, `/conversations`, `/stats` | ✓ **New** |

**`controllers/dto/`** — Request bodies:
- [`SingleStateAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/SingleStateAgentCreateDTO.java) — PROMISE original
- [`BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) — ✓ **New** (with `language` and `nickname` fields)

**`controllers/views/`** — Response classes:
- [`AgentInfoView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/AgentInfoView.java)
- [`ResponseView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/ResponseView.java)
- [`TTSRequest.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/TTSRequest.java) ✓ **New**
- [`UserAgentView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserAgentView.java) ✓ **New**
- [`UserConversationView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserConversationView.java) ✓ **New**

### `logging/` — Live Log Streaming (✓ entirely new in Oblivio)

| File | What It Does |
|---|---|
| [`logging/LogEvent.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogEvent.java) | DTO for log events |
| [`logging/SseLogAppender.java`](src/main/java/ch/zhaw/statefulconversation/logging/SseLogAppender.java) | Custom Logback appender, captures all logs |
| [`logging/LogStreamBroadcaster.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamBroadcaster.java) | Broadcasts logs to all SSE subscribers |
| [`logging/LogStreamController.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamController.java) | REST endpoint `GET /logs/stream` for real-time browser-based log viewing |

### `model/` — Domain Model (PROMISE State Machine Core)

| File | What It Does | Adapted? |
|---|---|---|
| [`model/Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) | Top-level container with `initialState`, `currentState`, `storage` | ✓ `userId` column added |
| [`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) | Conversation state with prompts, utterances, transitions | ✓ `compactIfNeeded()` call added + TEXT columns |
| [`model/Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java) | Base entity for all prompt-like classes (uses SINGLE_TABLE inheritance) | ✓ TEXT column instead of VARCHAR(10000) |
| [`model/Utterance.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterance.java) | A single message in the conversation | ✓ TEXT column |
| [`model/Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java) | Conversation history + **Context Compaction** logic | ✓ `compactIfNeeded()` method added (~60 lines) |
| [`model/Transition.java`](src/main/java/ch/zhaw/statefulconversation/model/Transition.java) | Connection between states with Decisions (Guards) and Actions | PROMISE original |
| [`model/Decision.java`](src/main/java/ch/zhaw/statefulconversation/model/Decision.java) | Abstract base for guards | PROMISE original |
| [`model/Action.java`](src/main/java/ch/zhaw/statefulconversation/model/Action.java) | Abstract base for transition side effects | PROMISE original |
| [`model/Final.java`](src/main/java/ch/zhaw/statefulconversation/model/Final.java) | End-state of the conversation | PROMISE original |
| [`model/Storage.java`](src/main/java/ch/zhaw/statefulconversation/model/Storage.java) | Key-value store on the agent (for block summaries) | PROMISE original |
| [`model/StorageEntry.java`](src/main/java/ch/zhaw/statefulconversation/model/StorageEntry.java) | Single key-value pair | PROMISE original |
| [`model/Response.java`](src/main/java/ch/zhaw/statefulconversation/model/Response.java), [`PromptResult.java`](src/main/java/ch/zhaw/statefulconversation/model/PromptResult.java) | Wrapper classes | PROMISE original |
| [`model/TransitionException.java`](src/main/java/ch/zhaw/statefulconversation/model/TransitionException.java) | Exception used for state transitions | PROMISE original |
| [`model/OuterState.java`](src/main/java/ch/zhaw/statefulconversation/model/OuterState.java) | Nested states (unused in Oblivio) | PROMISE original |

**`model/commons/actions/`** — Reusable action types (all PROMISE originals):
- [`StaticExtractionAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticExtractionAction.java) — Extracts data to storage (used for block summaries)
- [`TransferUtterancesAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/TransferUtterancesAction.java) — Copies utterances between states
- [`StaticSummarisationAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticSummarisationAction.java)
- [`RemoveLastUtteranceAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/RemoveLastUtteranceAction.java)
- [`DynamicExtractionAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/DynamicExtractionAction.java)

**`model/commons/decisions/`** — Reusable decision types (all PROMISE originals):
- [`StaticDecision.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/decisions/StaticDecision.java) — Fixed guard prompt
- [`DynamicDecision.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/decisions/DynamicDecision.java)

### `repositories/` — JPA Database Access (all PROMISE originals)

| File | What It Does |
|---|---|
| [`AgentRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/AgentRepository.java) | CRUD for `agent` table |
| [`StateRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/StateRepository.java) | CRUD for `state` table |
| [`StorageRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/StorageRepository.java) | CRUD for `storage` table |
| [`StorageEntryRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/StorageEntryRepository.java) | CRUD for `storage_entry` table |
| [`UtteranceRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/UtteranceRepository.java) | CRUD for `utterance` table |
| [`UtterancesRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/UtterancesRepository.java) | CRUD for `utterances` table |

### `spi/` — Service Provider Interface (OpenAI integration)

| File | What It Does | Adapted? |
|---|---|---|
| [`spi/LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) | Central GPT-4o bridge — `complete()`, `decide()`, `extract()`, `summarise()` | ✓ `summariseOffline()` added |
| [`spi/OpenAIProperties.java`](src/main/java/ch/zhaw/statefulconversation/spi/OpenAIProperties.java) | Loads API key from properties | PROMISE original |
| [`spi/ContenFilterException.java`](src/main/java/ch/zhaw/statefulconversation/spi/ContenFilterException.java) | Exception for OpenAI content filter | PROMISE original |
| [`spi/GsonExclude.java`](src/main/java/ch/zhaw/statefulconversation/spi/GsonExclude.java) | Annotation to exclude JPA fields from JSON | PROMISE original |

### Configuration Files

| File | What It Does | Adapted? |
|---|---|---|
| [`resources/application.properties.template`](src/main/resources/application.properties.template) | Template for local DB connection | — |
| [`resources/application-prod.properties`](src/main/resources/application-prod.properties) | **Production config — reads Railway env vars for Supabase** | ✓ **New** |
| [`resources/openai.properties.template`](src/main/resources/openai.properties.template) | Template for local OpenAI key | — |
| [`resources/openai-prod.properties`](src/main/resources/openai-prod.properties) | Production OpenAI config | ✓ **New** |
| [`resources/logback-spring.xml`](src/main/resources/logback-spring.xml) | Logging config | ✓ SSE appender registered |

### Database Scripts ([`sql/`](sql/))

| File | What It Does |
|---|---|
| [`sql/SUPABASE_TABLES.sql`](sql/SUPABASE_TABLES.sql) | Creates Oblivio-specific tables: `user_agents`, `user_legacies`, `legacy_access_codes`, `legacy_messages` |
| [`sql/supabase_migrations.sql`](sql/supabase_migrations.sql) | Later migrations (e.g. `visitor_name`, `user_id` columns added to `legacy_messages`) |

### Documentation ([`docs/`](docs/))

| File | What It Covers |
|---|---|
| [`docs/OBLIVIO_BUILD_GUIDE.md`](docs/OBLIVIO_BUILD_GUIDE.md) | **Complete technical guide** — PROMISE introduction, all extensions, step-by-step rebuild instructions |
| [`docs/OBLIVIO_GITHUB_GUIDE.md`](docs/OBLIVIO_GITHUB_GUIDE.md) | **Repository tour** — what every file does, deployment workflow |
| [`docs/QUICKSTART.md`](docs/QUICKSTART.md) | Fast setup guide |
| [`docs/PROMISE_INTEGRATION_GUIDE.md`](docs/PROMISE_INTEGRATION_GUIDE.md) | How Oblivio integrates PROMISE |
| [`docs/FRONTEND_INTEGRATION_STEPS.md`](docs/FRONTEND_INTEGRATION_STEPS.md) | Frontend setup steps |
| [`docs/ELEVENLABS_SETUP.md`](docs/ELEVENLABS_SETUP.md) | TTS configuration |
| [`docs/RAILWAY_SUPABASE_DEPLOYMENT.md`](docs/RAILWAY_SUPABASE_DEPLOYMENT.md) | Deployment instructions |
| [`docs/USER_TRACKING_FEATURES.md`](docs/USER_TRACKING_FEATURES.md) | Multi-user tracking |
| [`docs/INTEGRATION_COMPLETE.md`](docs/INTEGRATION_COMPLETE.md) | All integration components |

---

## How GitHub → Railway → Supabase works

The deployment is a fully automated push-to-deploy model. Here's the complete flow:

### 1. Code Push to GitHub

```
Developer makes changes locally
        │
        ▼
git push origin main
        │
        ▼
GitHub registers the push, fires webhook to Railway
```

### 2. Railway Builds the Container

```
Railway reads Dockerfile
        │
        ▼
Stage 1: JDK + Maven Wrapper → compiles JAR (`./mvnw clean package -DskipTests`)
        │
        ▼
Stage 2: JRE + JAR → final runtime image
        │
        ▼
Container starts: `java -jar app.jar`
```

### 3. Application Connects to Supabase

When the Java app starts, it reads [`application-prod.properties`](src/main/resources/application-prod.properties):

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

The `${...}` placeholders are filled by Railway from environment variables. **Passwords never live in the repo.**

Spring Boot uses **JDBC** to open a connection pool (HikariCP, 5 connections) to Supabase PostgreSQL.

### 4. Hibernate Manages Tables Automatically

`spring.jpa.hibernate.ddl-auto=update` means Hibernate inspects all `@Entity` classes and creates/updates tables accordingly:

| Table | Created By | Used By |
|---|---|---|
| `agent`, `state`, `prompt` | **Hibernate (automatic)** | Backend |
| `transition`, `prompt_transitions` | **Hibernate** | Backend |
| `utterance`, `utterances` | **Hibernate** | Backend |
| `storage`, `storage_entry` | **Hibernate** | Backend |
| `user_agents` | Manual via [`SUPABASE_TABLES.sql`](sql/SUPABASE_TABLES.sql) | Frontend |
| `user_legacies` | Manual | Frontend |
| `legacy_access_codes` | Manual | Frontend |
| `legacy_messages` | Manual | Frontend |

**Important caveat:** Hibernate's `update` mode adds new columns but does NOT change column types. When Oblivio migrated from `VARCHAR(10000)` to `TEXT` (for long persona prompts), this had to be done manually:
```sql
ALTER TABLE prompt ALTER COLUMN prompt TYPE TEXT;
```

### 5. The Full Request Flow (Single Message Example)

When a visitor sends a message in the Legacy Chat:

```
1. Browser → POST https://promise-production.up.railway.app/{agentId}/respond
              body: { "content": "Tell me about your childhood" }
              
2. Railway: AgentController.respond() handles the request
              
3. Spring + JPA: Loads Agent + all states + all utterances from Supabase

4. Agent.respond("Tell me...") → State.respond(...)
              
5. State.respond() calls Utterances.compactIfNeeded()
   ├── if >20 user messages, calls LMOpenAI.summariseOffline()
   ├──    → OpenAI API call to summarize old messages
   └── replaces old messages with one system message

6. State.respond() calls LMOpenAI.complete()
   └── → OpenAI API call to generate the persona's response

7. Persona response is added to utterances

8. State.raiseIfTransit() checks all transitions
   └── each Decision calls LMOpenAI.decide() → true/false from GPT
   
9. If guard returns true → Transition fires:
   ├── StaticExtractionAction.execute() saves data to storage
   └── currentState = subsequentState

10. Repository.save(agent) → JPA writes everything back to Supabase

11. Response returned to browser → also saved to legacy_messages by frontend
```

### 6. Frontend Deployment (Separate Workflow)

The frontend is **not on GitHub**. It's uploaded manually via FTP to Hostpoint:

```
Local: edit Website/legacy.html
        │
        ▼
FTP-Client uploads to Hostpoint server
        │
        ▼
Live on https://oblivio.ch
```

---

## What Was Adapted From PROMISE

Oblivio is built on top of [PROMISE](https://github.com/2024-ZHAW-PM4), a state-machine framework for LLM conversations developed at ZHAW. Below is a summary of every modification.

### Unchanged from PROMISE (~30 files)

All core state-machine classes work as in PROMISE: `Agent`, `Transition`, `Decision`, `Action`, `Storage`, `StorageEntry`, `Response`, `PromptResult`, `Final`, `OuterState`, `TransitionException`, all of `model/commons/*`, all `repositories/*`, `OpenAIProperties`, `ContenFilterException`, `GsonExclude`, `AgentController`, `StatefulconversationApplication`.

### Slightly Modified

| File | What Changed | Why |
|---|---|---|
| [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) | Added `utterances.compactIfNeeded()` call in `respond()` | Token cost optimization |
| [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) | `starterPrompt` and `summarisePrompt` → TEXT | Long persona prompts (>10k chars) |
| [`Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java) | `prompt` column → TEXT | Long persona prompts |
| [`Utterance.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterance.java) | `content` column → TEXT | Long LLM responses |
| [`Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) | Added `userId` field | Multi-user support |
| [`AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) | Added `/agent/biographer` endpoint | New agent type |
| [`AgentMetaType.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaType.java) | Added `biographer = 1` enum value | New agent type |

### Heavily Extended

| File | What Was Added | Lines Added |
|---|---|---|
| [`AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java) | `createBiographerAgent()` + 70 block prompts in 8 languages + helpers | ~500 lines |
| [`Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java) | `compactIfNeeded()` method for token-cost optimization | ~60 lines |
| [`LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) | `summariseOffline()` method for plain-text summaries | ~10 lines |

### Completely New (11 files)

| File | Purpose |
|---|---|
| [`config/WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) | CORS configuration |
| [`controllers/TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) | ElevenLabs TTS bridge |
| [`controllers/UserLogController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java) | Multi-user tracking endpoints |
| [`controllers/dto/BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) | Biographer creation DTO |
| [`controllers/views/TTSRequest.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/TTSRequest.java) | TTS request DTO |
| [`controllers/views/UserAgentView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserAgentView.java) | Multi-user response |
| [`controllers/views/UserConversationView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserConversationView.java) | Multi-user response |
| [`logging/LogEvent.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogEvent.java) | Log event DTO |
| [`logging/LogStreamBroadcaster.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamBroadcaster.java) | SSE broadcaster |
| [`logging/LogStreamController.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamController.java) | `GET /logs/stream` endpoint |
| [`logging/SseLogAppender.java`](src/main/java/ch/zhaw/statefulconversation/logging/SseLogAppender.java) | Logback appender |

Plus all **infrastructure files**: [`Dockerfile`](Dockerfile), [`railway.json`](railway.json), [`.railwayignore`](.railwayignore), [`application-prod.properties`](src/main/resources/application-prod.properties), [`openai-prod.properties`](src/main/resources/openai-prod.properties), [`.env.example`](.env.example), [`CITATION.cff`](CITATION.cff).

---

## API Overview

Live API: `https://promise-production.up.railway.app`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/actuator/health` | Health check |
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
| `GET` | `/user/{userId}/agents` | List user's agents |
| `GET` | `/user/{userId}/conversations` | User's conversations |
| `GET` | `/user/{userId}/stats` | Multi-user statistics |
| `GET` | `/logs/stream` | Server-Sent Events log stream |

---

## Local Development

### Prerequisites
- Java 21 (JDK)
- Maven (or use the included Maven Wrapper `mvnw`)
- PostgreSQL (local or remote)
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
