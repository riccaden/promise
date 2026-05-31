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

[![Personas](https://img.shields.io/badge/Personas-15-orange)](#concept)
[![Languages](https://img.shields.io/badge/Languages-8-blue)](#languages)
[![Variants](https://img.shields.io/badge/Conversation_Variants-3-green)](#concept)
[![Blocks](https://img.shields.io/badge/Biographer_Blocks-10-purple)](#concept)
[![States](https://img.shields.io/badge/PROMISE_States-21-yellow)](#what-was-adapted-from-promise)
[![License](https://img.shields.io/badge/License-Academic-lightgrey)](#author)

---

*Bachelor's Thesis · ZHAW School of Management and Law · Business Informatics · 2026*

</div>

---

## Table of Contents

1. [Project Status](#project-status)
2. [Why Oblivio?](#why-oblivio)
3. [What is Oblivio?](#what-is-oblivio)
4. [Concept](#concept)
5. [Languages](#languages)
6. [Key Features](#key-features)
7. [Architecture](#architecture)
8. [Tech Stack](#tech-stack)
9. [Repository Walkthrough](#repository-walkthrough) ← **start here if you want to navigate the code**
10. [How GitHub → Railway → Supabase works](#how-github--railway--supabase-works)
11. [What Was Adapted From PROMISE](#what-was-adapted-from-promise) ← **17-step rebuild guide**
12. [Website Setup — What the Frontend Needs to Run](#website-setup--what-the-frontend-needs-to-run)
13. [API Overview](#api-overview)
14. [Local Development](#local-development)
15. [Deployment](#deployment)
16. [Author](#author)

---

## Project Status

> **Status:** Active research project — Bachelor's thesis in progress.
> **Live platform:** [oblivio.ch](https://oblivio.ch)
> **API endpoint:** [promise-production.up.railway.app](https://promise-production.up.railway.app)
> **Study participants:** 15 personas captured across 4 native languages
> **Last updated:** May 2026

### At a Glance

| Metric | Value |
|---|---|
| Biographer thematic blocks | 10 |
| PROMISE states per Biographer | 21 (10 × 2 + Final) |
| Conversation variants per persona | 3 |
| Supported languages | 8 |
| Study participants → personas | 15 |
| Java classes (backend) | 70+ |
| Frontend HTML pages | 16 |
| Translation keys per language | ~400 |
| Context-compaction threshold | 20 user messages |

---

## Why Oblivio?

Everyone has a story worth preserving. But traditional methods — written biographies, video recordings, photo albums — capture *content* without capturing *personality*. They are static. You can read someone's words, but you can't have a new conversation with them.

Oblivio changes this. It uses conversational AI to not only **record** a life story, but to **recreate the way someone speaks, thinks, and feels** — so that loved ones can continue talking to that person's digital essence long after the original conversation ends.

> *"Not a form. Not a questionnaire. A conversation that actually listens."*

---

## What is Oblivio?

Oblivio is a web application for digitally preserving life stories. People tell their story through a guided AI interview (**Biographer Agent**) — from which an interactive digital persona (**Legacy Agent**) is created that loved ones can talk to in the future.

Built as a bachelor's thesis at [ZHAW](https://www.zhaw.ch), powered by the [PROMISE Framework](https://github.com/zhaw-iwi/promise) for state-based conversational AI.

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

In total, **15 personas** were created from study participants across **4 native languages** (German, Italian, Japanese, Korean), each available in three conversation variants.

**Three conversation variants:**

| Variant | Name | Description |
|:-------:|------|-------------|
| 1 | **Analysis** | Persona waits, with personality analysis embedded (radar chart, communication DNA, life pattern) |
| 2 | **Active** | Persona greets first and leads the conversation |
| 3 | **Passive** | Persona waits silently, responds only when spoken to |

Visitors can switch between variants at any time during a conversation; the message history is preserved across mode switches.

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

Oblivio is built on top of [PROMISE](https://github.com/zhaw-iwi/promise), a state-machine framework for LLM conversations developed at ZHAW. This section is a **step-by-step guide** to recreate Oblivio from a fresh PROMISE clone — with concrete before/after code comparisons.

---

### Step 1: Fork PROMISE and Set Up Local Environment

```bash
git clone https://github.com/zhaw-iwi/promise.git oblivio-backend
cd oblivio-backend
```

Required tools:
- Java 21 (JDK)
- Maven Wrapper (included)
- PostgreSQL access (local or Supabase)
- OpenAI API Key
- (Optional) ElevenLabs API Key

---

### Step 2: Swap MySQL for PostgreSQL in [`pom.xml`](pom.xml)

**Why this change?**
PROMISE was originally configured for MySQL. Oblivio needed a hosted database with built-in authentication and JWT support — Supabase was chosen because it offers PostgreSQL plus user management out of the box, removing the need to write a separate auth service. PostgreSQL also handles JSONB and TEXT columns better than MySQL, which matters for our long persona prompts and structured block summaries.

**Before (PROMISE):**
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**After (Oblivio):**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Without this change:** Spring Boot would fail at startup with `Driver org.postgresql.Driver claims to not accept jdbcUrl` because the PostgreSQL driver wouldn't be on the classpath.

---

### Step 3: Migrate Database Columns to TEXT

**Why this change?**
PROMISE caps prompt columns at VARCHAR(10000). Oblivio's persona prompts are much longer because they contain six sections (IDENTITY + CHAPTERS + ANALYSIS + STYLE + EXAMPLES + SELF_KNOWLEDGE + RULES) — a single persona prompt can reach 20,000 characters. Without this migration, inserting such prompts crashes with `value too long for type character varying(10000)`. Switching to PostgreSQL's TEXT type removes the size limit entirely without performance penalties.

Three Java entity files must be modified:

**Before (in [`Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java), [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java), [`Utterance.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterance.java)):**
```java
@Column(length = 10000)
private String prompt;
```

**After:**
```java
@Column(columnDefinition = "TEXT")
private String prompt;
```

**Important caveat — why this isn't automatic:** Hibernate's `ddl-auto=update` mode adds new columns but does NOT modify existing column types. If the database was previously running with VARCHAR(10000), the Java annotation alone won't change the schema. Run this manually in Supabase SQL Editor:
```sql
ALTER TABLE prompt ALTER COLUMN prompt TYPE TEXT;
ALTER TABLE state ALTER COLUMN starter_prompt TYPE TEXT;
ALTER TABLE state ALTER COLUMN summarise_prompt TYPE TEXT;
ALTER TABLE utterance ALTER COLUMN content TYPE TEXT;
```

---

### Step 4: Add `userId` to [`Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) for Multi-User Tracking

**Why this change?**
PROMISE was designed for a single user — every agent in the database belonged to "the user". Oblivio is a multi-user platform: many people simultaneously run their own Biographer sessions and own their own Legacy personas. Without a `userId` field, there would be no way to filter "show me only my agents" — anyone could potentially see everyone else's conversations. Adding this column to the `agent` entity links each agent to a Supabase Auth user via UUID.

**Add to `Agent` class:**
```java
// User-ID for Multi-User tracking
private String userId;

public String getUserId() { return this.userId; }
public void setUserId(String userId) { this.userId = userId; }
```

**Without this change:** The `/user/{userId}/agents` endpoint (Step 12) would have no field to filter by — and the frontend dashboard couldn't show users their own biographer history.

---

### Step 5: Add Context Compaction to [`Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java)

**Why this change?**
In PROMISE, every new user message resends the full conversation history to GPT — token costs grow linearly with conversation length. A 50-message conversation sends ~5000 input tokens on every turn, multiplied by every additional message. At GPT-4o pricing, a long biographer session can quickly cost several dollars. Beyond cost, GPT-4o's 128k token context window would eventually overflow for very long conversations, causing API errors.

The compaction mechanism solves both problems: after 20 user messages, older messages are summarized into a single system message (3–5 sentences). This keeps the cost roughly constant regardless of conversation length, while still preserving the gist of what was discussed earlier. The threshold of 20 was chosen empirically — early enough to keep costs manageable, late enough that natural conversation context is preserved.

**Add constants and method:**
```java
private static final int USER_MESSAGE_COMPACT_THRESHOLD = 20;
private static final int MESSAGES_TO_KEEP = 10;

public void compactIfNeeded() {
    long userCount = utteranceList.stream()
        .filter(u -> "user".equals(u.getRole())).count();
    if (userCount <= USER_MESSAGE_COMPACT_THRESHOLD) return;

    // Skip if already compacted
    if (!utteranceList.isEmpty()
        && "system".equals(utteranceList.get(0).getRole())
        && utteranceList.get(0).getContent().startsWith("[Zusammenfassung")) return;

    int splitPoint = utteranceList.size() - MESSAGES_TO_KEEP;
    // ... build text from older messages, call LMOpenAI.summariseOffline()
    // ... replace old messages with one system message
}
```

Full implementation: ~60 lines. See [`Utterances.java:118-179`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java).

---

### Step 6: Trigger Compaction in [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java)

**Why this change?**
The `compactIfNeeded()` method from Step 5 doesn't run by itself — it needs to be called at the right moment. The best moment is **right before each new LLM call**, so the compacted context is used in the next request. By placing it in `State.respond()` immediately after `acknowledge()` (which adds the new user message), every conversation in every state automatically benefits — Biographer, Legacy, even future agent types. One line, one place, full coverage.

**Before (PROMISE `State.respond()`):**
```java
public Response respond(String userSays, String outerPrompt) throws TransitionException {
    this.acknowledge(userSays, outerPrompt);
    String totalPrompt = this.composeTotalPrompt(outerPrompt);
    // ...
}
```

**After (one line added):**
```java
public Response respond(String userSays, String outerPrompt) throws TransitionException {
    this.acknowledge(userSays, outerPrompt);
    this.utterances.compactIfNeeded();  // ← Oblivio addition
    String totalPrompt = this.composeTotalPrompt(outerPrompt);
    // ...
}
```

---

### Step 7: Add `summariseOffline()` to [`LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java)

**Why this change?**
PROMISE's existing `summarise()` method returns a JSON object (designed for structured data extraction). But for context compaction, we need a plain-text paragraph to inject as a system message — JSON would confuse the LLM in subsequent turns. The new `summariseOffline()` method skips the JSON-format instruction and returns a raw string, ready to be wrapped in `[Zusammenfassung des bisherigen Gesprächs]` and prepended to the message list.

**Add:**
```java
public static String summariseOffline(Utterances utterances, String systemPrepend) {
    if (utterances.isEmpty()) throw new RuntimeException("...");
    List<Utterance> totalPrompt = composePromptCondensed(utterances, systemPrepend);
    return openai(totalPrompt, 0.0f, 0.0f);  // returns raw string
}
```

---

### Step 8: Build the Biographer Factory ([`AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java))

**Why this change?**
PROMISE provides only the building blocks (State, Transition, Decision, Action) — it does NOT provide ready-made agents. To create the Biographer, every block had to be hand-wired: a conversation state for asking questions, a confirmation state for validating the summary, transitions between them with the right guards, and an extraction action that saves the block summary to storage. Doing this 10 times manually would be error-prone, so the logic was centralized in one factory method. The factory also handles language selection (8 languages) and nickname injection so the same code produces customized biographers per user. **The chain is built backwards** because each transition needs to reference its `subsequentState`, which must exist at creation time — starting from Final and working back to Block 1 is the only practical way.

This is the most complex addition (~500 lines). The Biographer is a chain of 21 states (10 blocks × 2 + Final).

**Key method:**
```java
public static Agent createBiographerAgent(BiographerAgentCreateDTO data) {
    var storage = new Storage();
    String[][] prompts = buildBlockPrompts(data.getLanguage(), data.getNickname());
    String[] blockNames = buildBlockNames();

    // Start at Final, build backwards
    State current = new Final("Biografie abgeschlossen",
                              getFinalPrompt(data.getLanguage()),
                              getFinalStarterPrompt(data.getLanguage(), data.getNickname()));

    for (int i = 9; i >= 0; i--) {
        // Build Confirm state
        Decision confirmGuard = new StaticDecision(prompts[i][5]);
        Action extract = new StaticExtractionAction(prompts[i][6], storage, "block" + (i + 1));
        Transition confirmT = new Transition(List.of(confirmGuard), List.of(extract), current);
        State confirmState = new State(prompts[i][3], blockNames[i] + " - Bestätigung",
                                       prompts[i][4], List.of(confirmT));

        // Build Conv state
        Decision convGuard = new StaticDecision(prompts[i][2]);
        Action transfer = new TransferUtterancesAction(confirmState);
        Transition convT = new Transition(List.of(convGuard), List.of(transfer), confirmState);
        State convState = new State(prompts[i][0], blockNames[i], prompts[i][1], List.of(convT));

        current = convState;
    }
    return new Agent(data.getAgentName(), data.getAgentDescription(), current, storage);
}
```

Plus helpers:
- `buildBlockPrompts(language, nickname)` — returns 2D array `prompts[10][7]` with 70 prompt components
- `buildBlockNames()` — 10 block names
- `getLanguageInstruction(language)` — language prefix for 8 languages
- `getFinalPrompt(language)`, `getFinalStarterPrompt(language, nickname)` — for the Final state

---

### Step 9: Add `/agent/biographer` Endpoint

**Why this change?**
PROMISE exposes only `POST /agent/singlestate`, which creates a basic single-state agent. The Biographer is fundamentally different — it has 21 states, accepts a language parameter, and accepts a nickname. Adding a dedicated endpoint keeps the API clean: the frontend simply calls `/agent/biographer` with the user's chosen language and gets back a fully wired agent. Sharing the `/agent/singlestate` endpoint would require overloading it with optional fields and runtime type-checks, which is messy.

In [`AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java), add:

```java
@PostMapping("agent/biographer")
public ResponseEntity<AgentInfoView> createBiographer(@RequestBody BiographerAgentCreateDTO data) {
    if (data == null || AgentMetaType.biographer.getValue() != data.getType()) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    Agent agent = AgentMetaUtility.createBiographerAgent(data);
    this.repository.save(agent);
    return new ResponseEntity<>(new AgentInfoView(agent.getId(), agent.getName(),
                                                   agent.getDescription(), agent.isActive()),
                                HttpStatus.OK);
}
```

Plus extend [`AgentMetaType.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaType.java) with `biographer = 1`.

Plus create [`BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) (extends `SingleStateAgentCreateDTO` with `language` and `nickname` fields).

---

### Step 10: Add CORS Configuration

**Why this change?**
The Oblivio frontend lives on `oblivio.ch` (Hostpoint), the backend on `promise-production.up.railway.app` (Railway). These are two different domains. By default, browsers enforce the Same-Origin Policy: a JavaScript fetch from one origin to another is blocked unless the server explicitly permits it. Without CORS headers from the backend, every API call from the frontend would fail with `Access-Control-Allow-Origin missing`. The CORS configuration tells the browser "yes, this Railway server accepts requests from any origin" — a single Spring Boot bean fixes this for all endpoints at once.

**Create new file [`config/WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java):**
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
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
```

---

### Step 11: Add TTS Bridge ([`TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java))

**Why this change?**
PROMISE is text-only — there's no audio. Oblivio's legacy personas needed voices to feel emotionally real (a grandmother's voice carries memory the way text cannot). ElevenLabs provides the voice synthesis. But we cannot call ElevenLabs directly from the browser, because that would expose the API key in the frontend code where anyone could copy it and run up our bill. The Backend-as-a-bridge pattern keeps the key on the server: the frontend sends plain text to our backend, and our backend calls ElevenLabs with the key and returns the MP3 bytes.

```java
@PostMapping("{agentID}/tts")
public ResponseEntity<byte[]> textToSpeech(@PathVariable String agentID,
                                            @RequestBody TTSRequest request,
                                            @RequestParam String voice_id) {
    String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voice_id
                 + "?output_format=mp3_44100_128";
    Map<String, Object> body = Map.of(
        "text", request.getText(),
        "model_id", "eleven_multilingual_v2",
        "voice_settings", Map.of("stability", 0.5, "similarity_boost", 0.75)
    );
    // ... RestTemplate POST, return MP3 bytes
}
```

Also create [`TTSRequest.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/TTSRequest.java) as the request DTO.

---

### Step 12: Add Multi-User Endpoints ([`UserLogController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java))

**Why this change?**
PROMISE's `AgentController` exposes operations per-agent (`/{agentId}/respond` etc.), but it has no endpoints per-user. Once we added `userId` to the Agent entity (Step 4), we need ways to query "show me all of user X's agents", "their full conversation history", and "their usage stats". The frontend journey-dashboard page needs these endpoints to render the user's overview.

```java
@GetMapping("/user/{userId}/agents")        // List user's agents
@GetMapping("/user/{userId}/conversations") // Their conversations
@GetMapping("/user/{userId}/stats")         // Statistics
```

Plus DTOs [`UserAgentView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserAgentView.java) and [`UserConversationView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserConversationView.java).

---

### Step 13: Add Live Log Streaming (`logging/` package)

**Why this change?**
When something goes wrong in production on Railway, getting logs traditionally requires SSH access or the Railway CLI — neither is convenient during a live debugging session. With the live log stream, we open `/logs/stream` in any browser and immediately see what the state machine is doing in real time. This was especially helpful while debugging the cascading-transition bug: watching guards fire in sequence revealed exactly which condition was wrong. Implemented with Server-Sent Events because they're unidirectional (server → browser only), require no special client libraries, and survive automatic reconnects.

Four new classes:

- [`LogEvent.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogEvent.java) — DTO
- [`SseLogAppender.java`](src/main/java/ch/zhaw/statefulconversation/logging/SseLogAppender.java) — Logback appender
- [`LogStreamBroadcaster.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamBroadcaster.java) — broadcaster
- [`LogStreamController.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamController.java) — `GET /logs/stream` endpoint

Register the appender in [`logback-spring.xml`](src/main/resources/logback-spring.xml).

---

### Step 14: Add Production Properties

**Why this change?**
PROMISE ships with only local-development properties — database URL, password, API key are hardcoded. Pushing those to GitHub would leak credentials. Spring Boot's "profiles" mechanism allows different property files per environment: `application-prod.properties` is loaded only when `SPRING_PROFILES_ACTIVE=prod` is set. By using `${ENV_VAR}` placeholders, the actual values come from Railway's environment variables at runtime — keeping passwords out of the repository entirely. The `prepareThreshold=0` setting at the bottom is essential because Supabase uses PgBouncer in transaction-pooling mode, which incompatibly handles prepared statements.

**Create [`application-prod.properties`](src/main/resources/application-prod.properties):**
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.data-source-properties.prepareThreshold=0  # PgBouncer-compatible
```

**Create [`openai-prod.properties`](src/main/resources/openai-prod.properties):**
```properties
openai.url=https://api.openai.com/v1/chat/completions
openai.api-key-header=Authorization
openai.api-key-prefix=Bearer
openai.key=${OPENAI_KEY}
openai.model=gpt-4o
```

---

### Step 15: Containerize for Railway

**Why this change?**
PROMISE has no Dockerfile — it's meant to be run locally with `mvn spring-boot:run`. Railway needs an image to deploy, so we package the application in a Docker container. The multi-stage build is deliberate: Stage 1 includes the full JDK and Maven (~700 MB) needed only to compile the JAR, Stage 2 contains just the JRE and the compiled JAR (~200 MB). The smaller final image starts faster on Railway, uses less memory, and is more secure (fewer attack vectors than a JDK). The `USER nobody` line follows the principle of least privilege — if a vulnerability is exploited, the attacker has no shell privileges. The healthcheck lets Railway automatically restart the container if it stops responding.

Create [`Dockerfile`](Dockerfile) (multi-stage build):
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
HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health || exit 1
CMD ["java", "-jar", "app.jar"]
```

Create [`railway.json`](railway.json):
```json
{
  "build": { "builder": "DOCKERFILE" },
  "deploy": {
    "startCommand": "java -jar app.jar",
    "healthcheckPath": "/actuator/health",
    "restartPolicyType": "ON_FAILURE"
  }
}
```

Create [`.railwayignore`](.railwayignore) to skip unnecessary files during build:
```
target/
.git/
docs/
sql/
*.md
```

---

### Step 16: Create Supabase Tables for Frontend

**Why this change?**
Hibernate automatically creates all PROMISE-related tables (agent, state, prompt, utterance, etc.) when the backend starts. But the frontend talks **directly to Supabase** via the JavaScript client (bypassing the backend) for things like login, persona lookup, and message persistence. Those tables don't have corresponding Java entities, so Hibernate doesn't know about them — they must be created manually. Splitting responsibility this way is intentional: it lets the frontend operate independently of the backend for read-heavy operations, reducing latency and load on Railway.

Run [`sql/SUPABASE_TABLES.sql`](sql/SUPABASE_TABLES.sql) once on Supabase. Creates the tables used by the frontend (not by Hibernate):

| Table | Purpose |
|---|---|
| `user_agents` | Links Supabase user → PROMISE agent ID |
| `user_legacies` | Stores final block summaries as JSONB |
| `legacy_access_codes` | Persona access codes with `legacy_data`, `voice_id`, `avatar_url` |
| `legacy_messages` | All legacy chat messages (filtered by `visitor_id`) |
| `questionnaire_answers` | Pre-survey (Block 0) responses |

---

### Step 17: Deploy to Railway

**Why Railway?**
Railway was chosen because it integrates seamlessly with GitHub: every push to `main` triggers a fresh build and rolling deployment automatically — no separate CI/CD scripts, no manual SSH, no downtime. Alternatives like AWS or Google Cloud would have required significantly more setup. Railway also handles SSL/HTTPS, automatic restarts on failure, and Docker builds out of the box. The combination of "git push = live in 3 minutes" was essential for iterating quickly during the bachelor's thesis.

1. Push code to GitHub
2. On Railway: "New Project" → "Deploy from GitHub" → select your fork
3. Set environment variables in Railway Dashboard:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://<project>.supabase.co:5432/postgres
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=...
   OPENAI_KEY=sk-proj-...
   ELEVENLABS_API_KEY=...
   SPRING_PROFILES_ACTIVE=prod
   ```
4. Railway auto-builds the Dockerfile and starts the container
5. Generate a public domain in Railway settings → done

From now on: every `git push origin main` triggers a new deployment automatically.

---

### Summary: Files Modified vs Added

| Category | Count | Examples |
|---|---|---|
| **Unchanged from PROMISE** | ~30 files | Agent, Transition, Storage, all `commons/*`, all `repositories/*` |
| **Slightly modified** | 7 files | Prompt, State, Utterance (TEXT), Agent (userId), AgentMetaController (+endpoint), AgentMetaType, State (+compactIfNeeded call) |
| **Heavily extended** | 3 files | AgentMetaUtility (+500), Utterances (+60), LMOpenAI (+10) |
| **Completely new (Java)** | 11 files | WebConfig, TTSController, UserLogController, 4× logging/, 4× DTOs/views |
| **Completely new (infra)** | 7 files | Dockerfile, railway.json, .railwayignore, .env.example, application-prod, openai-prod, CITATION.cff |

---

## Website Setup — What the Frontend Needs to Run

The frontend (`Website/` folder) is **not on GitHub** — it lives separately and is uploaded via FTP to Hostpoint. But to recreate Oblivio, you need the same structure.

### Required Directory Layout

```
Website/                          ← Upload entire folder to your hosting
├── index.html                    Landing page
├── biographer.html               Biographer UI (Pre-Survey + Chat)
├── legacy.html                   Legacy chat UI (3 variants)
├── journey.html                  User dashboard (own personas)
├── signup.html, login.html       Supabase Auth pages
├── about.html, faq.html          Marketing pages
├── pricing.html, features.html
├── blog.html, contact.html
├── privacy.html, terms.html, security.html
├── 404.html
├── sitemap.xml, robots.txt
├── .htaccess                     Redirects (optional)
├── audio/
│   └── background.mp3            Ambient sound
├── images/
│   ├── logo.png, og-image.jpg
│   └── avatars/
│       └── *.jpg                 One photo per persona
└── js/
    ├── config.js                 Supabase URL + Anon Key + Backend URL
    ├── translations.js           i18n engine
    ├── lang-de.js, lang-en.js, lang-fr.js, lang-it.js,
    ├── lang-tr.js, lang-ko.js, lang-ja.js, lang-zh.js   8 language files
    ├── biographer-promise.js     Biographer API client
    └── legacy-chat.js            Legacy chat API client + visitor context
```

### Critical Frontend Files

| File | What It Must Contain |
|---|---|
| `js/config.js` | Three URLs: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `PROMISE_API_URL` (your Railway URL) |
| `js/legacy-chat.js` | `buildLegacySystemPrompt()`, `createLegacyAgent()`, `sendLegacyMessage()`, visitor context builder |
| `js/biographer-promise.js` | API client for Biographer (create, respond, fetch state) |
| `js/translations.js` | i18n engine that loads `lang-<code>.js` and replaces `data-i18n` attributes |
| `js/lang-*.js` | ~400 translation keys per language |
| `legacy.html` | Mode toggle (3 buttons), avatar, voice playback, localStorage persistence |
| `biographer.html` | Pre-Survey (Block 0), language picker, progress bar, completion card |

### Required Browser Libraries (via CDN, no NPM)

```html
<!-- Supabase JS Client -->
<script src="https://cdn.jsdelivr.net/npm/@supabase/supabase-js@2"></script>

<!-- Google Fonts -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@300;400;600&family=DM+Serif+Display&display=swap" rel="stylesheet">
```

That's it — no build tools, no NPM, no bundler. Pure HTML + Vanilla JS.

### Frontend Upload Workflow

```bash
# After making local changes to Website/legacy.html or js/lang-*.js:

1. Open FTP client (FileZilla, Cyberduck)
2. Connect to Hostpoint SFTP
3. Upload changed files to web root
4. Refresh browser → changes live on https://oblivio.ch
```

No staging, no CI. Bewusst einfach gehalten.

### Frontend-Backend Connection

The frontend talks to two backends:

**Supabase (direct, via JS client):**
- Login / Signup
- Read `legacy_access_codes` (persona prompts, avatar, voice_id)
- Write `legacy_messages` (every chat message)
- Read/write `user_agents`, `user_legacies`

**Railway (PROMISE backend, via fetch):**
- `POST /agent/biographer` to start a Biographer
- `POST /agent/singlestate` to create a Legacy agent
- `POST /{agentId}/respond` for every chat turn
- `POST /{agentId}/tts?voice_id=...` for voice playback

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
