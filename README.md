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

[![Personas](https://img.shields.io/badge/Personas-15-orange)](#end-to-end-process)
[![Languages](https://img.shields.io/badge/Languages-8-blue)](#end-to-end-process)
[![Variants](https://img.shields.io/badge/Conversation_Variants-3-green)](#end-to-end-process)
[![Built_on](https://img.shields.io/badge/Built_on-PROMISE_Framework-purple)](https://github.com/zhaw-iwi/promise)

---

*Bachelor's Thesis · ZHAW School of Management and Law · Business Informatics · 2026*

</div>

---

## What is Oblivio?

Oblivio is a web platform that lets people **preserve their life story as an interactive AI persona**. Through a guided AI interview across **10 thematic blocks**, the system captures someone's personality, memories, communication style, and values. From that data, a **digital persona** is generated — one that loved ones can chat with anytime, in **8 languages** and **3 conversation variants**.

Built on the [PROMISE Framework](https://github.com/zhaw-iwi/promise) (ZHAW Institute for Applied Information Technology), Oblivio extends it with multi-user support, mehrsprachigkeit, an end-to-end persona pipeline, and full deployment on Railway + Supabase + Hostpoint.

### Three Components in Production

| Component | Hosting | URL | Role |
|---|---|---|---|
| **Frontend** | Hostpoint (Swiss) | [oblivio.ch](https://oblivio.ch) | UI, i18n, authentication flow |
| **Backend (PROMISE + Oblivio extensions)** | Railway | [promise-production.up.railway.app](https://promise-production.up.railway.app) | State machine, REST API, LLM calls |
| **Database + Auth** | Supabase | `<project>.supabase.co` | PostgreSQL, JWT auth, RLS |

Plus two external APIs: **OpenAI GPT-4o** (LLM) and **ElevenLabs** (voice synthesis).

### At a Glance

- **21 states** in the Biographer (10 blocks × 2 + Final)
- **15 personas** captured from study participants
- **70+ Java classes** in the backend
- **17 HTML pages** in the frontend (sanitised template at [`Website-template/`](Website-template/))
- **8 languages** (DE, EN, FR, IT, TR, KO, JA, ZH)
- **Context compaction** after 20 messages (keeps token costs flat)

### Reading the rest of this README

The remainder of this README is one large guided tour: the complete **end-to-end process** from a user signing up to loved ones chatting with the persona. For every step you'll see:

1. **What happens** (user-facing description)
2. **Frontend files** involved (HTML or JS)
3. **Backend files** involved (Java controllers, state machine classes, etc.)
4. **What had to be added to PROMISE** to make this step work
5. **Where the data flows** — what reaches Supabase, what goes to Railway, what stays in the browser

---

## End-to-End Process

The full journey from "user registers" to "loved ones chat with the digital persona", in **two perspectives**:

- **Perspective 1 — The Persona Owner:** Someone who registers on oblivio.ch, takes the Biographer interview, and generates a digital persona of themselves
- **Perspective 2 — The Visitor:** A loved one who receives an access code and chats with that persona

---

## Perspective 1: The Persona Owner

### Step 1 — Sign Up / Log In

**What happens:** User opens oblivio.ch, clicks Sign Up, enters email and password, verifies via email, then logs in.

**Frontend files:**
- [`Website-template/signup.html`](Website-template/signup.html) — signup form
- [`Website-template/login.html`](Website-template/login.html) — login form
- Both files call the Supabase JS client directly (no backend involved)

**Backend / Railway:**
- Not involved in this step. Supabase Auth handles registration entirely on its own.

**PROMISE adaptations:**
- None — authentication is handled by Supabase, not by PROMISE. PROMISE had no auth system; Oblivio relies entirely on Supabase Auth.

**Where the data goes:**
- Supabase table `auth.users` (managed automatically by Supabase)
- JWT token stored in browser cookie + localStorage

---

### Step 2 — Choose Language

**What happens:** User picks one of 8 languages from a dropdown. The whole UI switches plus the Biographer interview is conducted in that language.

**Frontend files:**
- [`Website-template/biographer.html`](Website-template/biographer.html) — language picker UI
- [`Website-template/js/translations.js`](Website-template/js/translations.js) — i18n engine
- [`Website-template/js/lang-de.js`](Website-template/js/lang-de.js), [`lang-en.js`](Website-template/js/lang-en.js), [`lang-fr.js`](Website-template/js/lang-fr.js), [`lang-it.js`](Website-template/js/lang-it.js), [`lang-tr.js`](Website-template/js/lang-tr.js), [`lang-ko.js`](Website-template/js/lang-ko.js), [`lang-ja.js`](Website-template/js/lang-ja.js), [`lang-zh.js`](Website-template/js/lang-zh.js) — translation tables (~400 keys each)

**Backend / Railway:**
- Not yet — language is recorded locally first, then passed to backend in Step 4.

**PROMISE adaptations:**
- PROMISE has no built-in language support. Oblivio added [`getLanguageInstruction()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L480) in `AgentMetaUtility.java` which returns a language-specific prefix prepended to every block prompt (e.g. "WICHTIG: Du MUSST auf Türkisch kommunizieren..."). The original German prompts stay, GPT-4o translates at runtime.

**Where the data goes:**
- `localStorage('oblivio_language')` — kept on the user's device only

---

### Step 3 — Block 0: Pre-Survey

**What happens:** Before the actual Biographer starts, the user fills out a short questionnaire: age range, gender, personality traits, communication style. This gives the Biographer agent context for personalisation.

**Frontend files:**
- [`Website-template/biographer.html`](Website-template/biographer.html) — the questionnaire form with dropdowns and multi-select fields

**Backend / Railway:**
- Not involved. The frontend writes directly to Supabase.

**PROMISE adaptations:**
- None — this is purely an Oblivio addition that happens before any PROMISE agent is created.

**Where the data goes:**
- Supabase table `questionnaire_answers` (JSONB column `answers` with all responses)
- Used in Step 5 as `nickname` and contextual hints for the Biographer prompts

---

### Step 4 — Biographer Agent is Created

**What happens:** Frontend sends a POST request to the backend asking for a new Biographer. The backend builds a 21-state agent (10 blocks × 2 states + Final) on the fly and returns its ID.

**Frontend files:**
- [`Website-template/biographer.html`](Website-template/biographer.html) — UI flow
- [`Website-template/js/biographer-promise.js`](Website-template/js/biographer-promise.js) — API client that calls `POST /agent/biographer`

**Backend / Railway:**
- [`controllers/AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) — the endpoint
- [`controllers/AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java) — factory method [`createBiographerAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64)
- [`controllers/dto/BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) — request body

**PROMISE adaptations:**
- **New endpoint:** `POST /agent/biographer` added to `AgentMetaController.java` (PROMISE only had `/agent/singlestate`)
- **New enum value:** `biographer = 1` added to [`AgentMetaType.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaType.java)
- **New DTO:** `BiographerAgentCreateDTO` includes `language` and `nickname` fields
- **New factory method:** `createBiographerAgent()` builds the 21-state chain **backwards** (Final → Block 10 → ... → Block 1) because each transition needs to reference its `subsequentState` at creation time
- **New helper:** [`buildBlockPrompts()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L143) — returns a 2D array `prompts[10][7]` with 70 prompts (10 blocks × 7 components each: Conv System Prompt, Conv Starter, Conv Guard, Confirm System Prompt, Confirm Starter, Confirm Guard, Extract Prompt)
- **New `userId` field** in [`Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) so the agent is linked to the Supabase user

**Where the data goes:**
- **Supabase (PROMISE-managed tables, written by Hibernate automatically):** `agent`, `state`, `prompt`, `transition`, `prompt_transitions`, `utterance`, `utterances`, `storage`, `storage_entry`
- **Supabase (Oblivio-managed):** Frontend writes a row to `user_agents` linking `user_id` → `agent_id`
- **Returned to frontend:** the new agent's UUID

---

### Step 5 — The 10 Blocks (Conversation + Confirmation)

**What happens:** For each of 10 thematic blocks, the user goes through two states: a **conversation state** (AI asks all the block's questions, e.g. "Tell me about your childhood…") and a **confirmation state** (AI summarises what it heard, user confirms or corrects).

#### 5a. Conversation Phase

**Frontend files:**
- [`Website-template/biographer.html`](Website-template/biographer.html) — chat UI, progress bar, voice-input button
- [`Website-template/js/biographer-promise.js`](Website-template/js/biographer-promise.js) — sends each message via `POST /{agentId}/respond`

**Backend / Railway:**
- [`controllers/AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) — endpoint handler `respond()`
- [`model/Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) — loads agent, delegates to current state
- [`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) — [`respond()`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L168) method
- [`model/Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java) — adds user message, calls [`compactIfNeeded()`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118)
- [`spi/LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) — calls OpenAI GPT-4o to generate the assistant response
- [`model/Transition.java`](src/main/java/ch/zhaw/statefulconversation/model/Transition.java) — checks after every message: should this transition fire?
- [`model/commons/decisions/StaticDecision.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/decisions/StaticDecision.java) — the Guard that asks GPT-4o "Have all questions been asked? true/false"

**PROMISE adaptations:**
- **Context Compaction (new method):** [`Utterances.compactIfNeeded()`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) — after 20 user messages, older messages are summarised via [`LMOpenAI.summariseOffline()`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) and replaced with one system message. Cuts token costs from linear to constant.
- **Trigger:** One added line in [`State.respond()`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L171) calls `compactIfNeeded()` before each LLM call
- **New LMOpenAI method:** `summariseOffline()` returns plain text (existing `summarise()` returns JSON, which would confuse subsequent turns)
- **TEXT columns:** [`Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java), [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java), [`Utterance.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterance.java) — PROMISE used `VARCHAR(10000)` which crashed on long persona prompts; switched to PostgreSQL `TEXT` (unlimited)
- **PostgreSQL driver in [`pom.xml`](pom.xml)** instead of MySQL (PROMISE used MySQL originally; Supabase requires PostgreSQL)

**Where the data goes:**
- Every message (user + assistant) → Supabase tables `utterance` / `utterances` (PROMISE-managed by Hibernate)
- Conversation state itself → Supabase table `state` and `agent.currentState`
- LLM call → OpenAI API (external service)
- Browser displays the assistant response

#### 5b. Transition: Conversation → Confirmation

When the Guard returns `true`, the transition fires:

**Backend / Railway:**
- [`model/Transition.java`](src/main/java/ch/zhaw/statefulconversation/model/Transition.java) — calls all decisions, then all actions
- [`model/commons/actions/TransferUtterancesAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/TransferUtterancesAction.java) — copies all conversation messages from the Conv state to the Confirm state so the Confirm state can summarise them

**PROMISE adaptations:** none — this transition is built with stock PROMISE classes; only the prompts are Oblivio-specific.

**Where the data goes:** A copy of `utterances` is created and linked to the Confirm state in Supabase.

#### 5c. Confirmation Phase

**What happens:** The AI summarises the block (e.g. "From what you told me, I gathered that..."). The user confirms or asks for corrections. On confirmation, the AI extracts a structured JSON summary and stores it.

**Backend / Railway:**
- Same `State.respond()` flow as above, but now in the Confirm state with its own prompts
- [`model/commons/actions/StaticExtractionAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticExtractionAction.java) — when the user confirms, this action runs `LMOpenAI.extract()` which returns structured JSON and stores it in the agent's `Storage` under the key `block1` / `block2` / ... / `block10`

**Where the data goes:**
- Supabase `storage_entry` table (PROMISE-managed) — the new JSON block summary

After Block 10's confirmation, the next transition leads to the Final state.

---

### Step 6 — All Blocks Complete: Final State + Access Code Generation

**What happens:** After Block 10 confirmation, the Biographer agent transitions to the Final state. The frontend now requests the 10 stored summaries, persists them in Supabase, and generates an access code for sharing.

**Frontend files:**
- [`Website-template/biographer.html`](Website-template/biographer.html) — handles completion UI (shows the access code with a "Copy" button and email-share button)

**Backend / Railway:**
- [`model/Final.java`](src/main/java/ch/zhaw/statefulconversation/model/Final.java) — Final state, `isActive()` returns `false`
- [`controllers/AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) — `GET /{agentId}/storage` returns all 10 block summaries as JSON

**PROMISE adaptations:**
- None at this point — but the new endpoint `GET /{agentId}/storage` is in the original PROMISE controller and is used by Oblivio to retrieve block summaries.

**Where the data goes:**
- **Supabase `user_legacies` table:** Frontend writes the 10 block summaries as one JSONB document (column `legacy_data`)
- **Supabase `legacy_access_codes` table:** Frontend generates an 8-character random code (e.g. `VDSRMACZ`) and inserts a row with `access_code`, `user_id`, `nickname`, `language`, `is_active = true`

---

### Step 7 — Persona Prompts Are Created (Manual Step)

**What happens:** From the 10 block summaries, three full persona prompts are crafted — one per conversation variant. This is currently a **manual admin step** done via SQL: the admin reads the block summaries, drafts the prompts, and inserts them into the `legacy_data` JSONB column.

Each prompt is built from these sections:

```
[SECTION:IDENTITY]        Who the person is, what language they speak, opening behaviour
[SECTION:CHAPTERS]        10 thematic summaries from the Biographer interview
[SECTION:ANALYSIS]        Personality radar, communication DNA, life pattern (Variant 1 only)
[SECTION:STYLE]           Language patterns, dialect, vocabulary, sentence structure
[SECTION:EXAMPLES]        Real dialogue examples to anchor the LLM's voice
[SECTION:SELF_KNOWLEDGE]  First-person paragraphs the persona "knows" about itself
[SECTION:RULES]           Behavioural constraints — no lists, no AI phrases, stay in character
```

**Frontend files:** none — manual SQL.

**Backend / Railway:** none.

**PROMISE adaptations:** none — this happens entirely in Supabase.

**Where the data goes:**
- Supabase `legacy_access_codes.legacy_data` JSONB column gets three new keys:
  - `full_prompt_active` — Variant 2 (persona greets first)
  - `full_prompt_passive` — Variant 3 (persona waits)
  - `full_prompt_analysis` — Variant 1 (with personality analysis block)

---

### Step 8 — Optional: Voice and Avatar Assignment

**What happens:** Optionally, the admin records or selects an [ElevenLabs](https://elevenlabs.io) voice for the persona and uploads a profile photo. These enable audio playback and a face for the chat avatar.

**Frontend files:** Avatar images are uploaded to Hostpoint at `/images/avatars/<name>.jpg` (referenced by `avatar_url`).

**Backend / Railway:** none for assignment; voice is used in Step 14 of Perspective 2.

**PROMISE adaptations:** none.

**Where the data goes:**
- Supabase `legacy_access_codes` gets `voice_id` (ElevenLabs voice identifier) and `avatar_url` (path to the avatar image on Hostpoint)

The persona is now fully online. The 8-character access code can be shared with loved ones.

---

## Perspective 2: The Visitor

### Step 9 — Open Legacy Chat with Access Code

**What happens:** A loved one goes to oblivio.ch/legacy.html, enters the 8-character access code, and the website loads the persona's data.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — code input form, then chat UI

**Backend / Railway:**
- Not involved. The frontend queries Supabase directly via the JS client.

**PROMISE adaptations:**
- None — this is purely frontend-to-Supabase.

**Where the data goes:**
- Frontend reads from Supabase `legacy_access_codes` table: `nickname`, `language`, `legacy_data` (with all 3 prompts), `avatar_url`, `voice_id`
- Returned data drives the rest of the chat experience

---

### Step 10 — Enter Visitor Info (Name, Relation, Gender)

**What happens:** Before chatting, the visitor enters their name (e.g. "Maria"), their relationship to the persona (child, friend, etc.), and their gender. The persona will know who's talking to it.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — visitor info form
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js) — handles the visitor info, builds the [Visitor Context block](Website-template/js/legacy-chat.js#L29) that will be injected into the persona prompt

**Backend / Railway:** none.

**PROMISE adaptations:** none.

**Where the data goes:**
- `localStorage('oblivio_visitor_<accessCode>')` as JSON: `{ name, relation, gender }`
- Used to personalise the persona prompt in Step 12

---

### Step 11 — Choose Conversation Variant

**What happens:** The visitor sees three buttons at the top of the chat:

```
[Variant 1]   [Variant 2]   [Variant 3]
 Analysis      Active        Passive
```

Default = Variant 1 (Analysis).

| Variant | Behaviour | Loaded from |
|:-:|---|---|
| **1 (Analysis)** | Persona waits silently. Includes personality analysis (radar, communication DNA, life pattern). | `legacy_data.full_prompt_analysis` |
| **2 (Active)** | Persona greets first with a personal hello. | `legacy_data.full_prompt_active` |
| **3 (Passive)** | Persona waits silently, no analysis. | `legacy_data.full_prompt_passive` |

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — mode toggle buttons (~line 1054)
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js) — `getScopedVisitorId()` adds `__active`, `__passive`, or `__analysis` suffix so the three conversations stay isolated

**Backend / Railway:** none yet.

**PROMISE adaptations:** none — the variants are an Oblivio concept layered on top of PROMISE. The backend treats all three identically: a single PROMISE state machine with a different prompt.

**Where the data goes:**
- `localStorage('oblivio_mode_<accessCode>')` = `'active'` / `'passive'` / `'analysis'`

---

### Step 12 — Build the System Prompt + Create Legacy Agent

**What happens:** The frontend assembles the final system prompt by combining (a) the right variant's `full_prompt_*` and (b) the Visitor Context block. Then it asks the backend to create a Single-State Agent.

**Frontend files:**
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js#L98) — [`buildLegacySystemPrompt()`](Website-template/js/legacy-chat.js#L98) composes the full prompt
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js#L29) — [`buildVisitorContext()`](Website-template/js/legacy-chat.js#L29) generates the visitor block in 7 languages
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js#L176) — [`createLegacyAgent()`](Website-template/js/legacy-chat.js#L176) calls `POST /agent/singlestate` to Railway

**Backend / Railway:**
- [`controllers/AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) — endpoint
- [`controllers/AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L20) — [`createSingleStateAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L20) builds a one-state agent with one transition to Final
- [`config/WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) — CORS configuration so the Hostpoint frontend is allowed to call Railway

**PROMISE adaptations:**
- **CORS configuration (new file):** [`WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) — PROMISE has no CORS, and browsers block cross-origin POSTs by default. Without this, the frontend on oblivio.ch could not call promise-production.up.railway.app at all.
- The `createSingleStateAgent()` method is from PROMISE, but Oblivio's `userId` extension means the Legacy agents can also be tracked per user.

**Where the data goes:**
- Supabase PROMISE-tables: new agent + state + utterances rows
- Returned to frontend: agent UUID

---

### Step 13 — Starter Message

**What happens:** The frontend calls `POST /{agentId}/start` to get the persona's opening message. Behaviour depends on the variant:

- **Variant 2 (Active):** Persona produces a real greeting in its own voice, e.g. *"Hey Maria, schön dich zu sehen!"*
- **Variants 1 & 3 (Analysis, Passive):** The starter prompt instructs the LLM to respond with literally the text `__WAIT__`. The frontend filters this out and shows a hint like *"Type to start the conversation"*.

**Frontend files:**
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js#L196) — `__WAIT__` filtering logic
- [`Website-template/legacy.html`](Website-template/legacy.html) — displays the greeting or hint

**Backend / Railway:**
- [`controllers/AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) — `start()` endpoint
- [`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) — `start()` method, calls `LMOpenAI.complete()` with the starter prompt

**PROMISE adaptations:**
- The `__WAIT__` token mechanism is an Oblivio invention. PROMISE's `State.start()` always generates a starter message. By passing a starter prompt that forces the LLM to emit only `__WAIT__`, Oblivio gets a valid PROMISE state with a (filtered) starter message — without showing a generic AI greeting to the visitor.

**Where the data goes:**
- The greeting (real or `__WAIT__`) is added to `utterances` in Supabase
- The frontend displays the real greeting in the chat or shows a hint instead

---

### Step 14 — The Conversation Loop

**What happens:** For every message the visitor types, the same flow runs:

1. Frontend sends `POST /{agentId}/respond`
2. Backend adds the message to the agent's `utterances`, runs `compactIfNeeded()`, calls GPT-4o for a response, checks if the visitor is saying goodbye (Guard), then returns the response
3. Frontend writes both messages (visitor + persona) to Supabase `legacy_messages`
4. Frontend displays the persona's response
5. Optionally: Frontend calls `POST /{agentId}/tts?voice_id=...` for spoken audio

**Frontend files:**
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js) — `sendLegacyMessage()` function
- [`Website-template/legacy.html`](Website-template/legacy.html) — `saveMessage()` (~line 1212) writes to Supabase; voice playback via `Audio(URL.createObjectURL(blob))`

**Backend / Railway:**
- [`controllers/AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) — `respond()` endpoint
- [`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L168) — state-machine logic
- [`model/Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) — context compaction
- [`spi/LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) — LLM call
- [`controllers/TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) — calls ElevenLabs API, returns MP3 bytes

**PROMISE adaptations:**
- **Context Compaction** (same as Step 5a) keeps cost low across long chats
- **TTS Controller (new file):** [`TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) — PROMISE has no audio output. Oblivio added this as a server-side bridge so the ElevenLabs API key stays on Railway (never exposed to the browser)
- **New DTO:** [`TTSRequest.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/TTSRequest.java)

**Where the data goes:**
- **Supabase PROMISE-tables (Hibernate):** Each message is stored as a row in `utterance`
- **Supabase Oblivio-tables (frontend):** Each message is *also* written to `legacy_messages` with: `access_code`, `visitor_id` (mode-scoped: `<uuid>__active` etc.), `visitor_name`, `user_id`, `role` (`user`/`legacy`), `content`, `created_at`
- **OpenAI API:** the GPT-4o call (text → text)
- **ElevenLabs API:** optional text-to-speech call (text → MP3)

The two parallel writes (PROMISE tables + `legacy_messages`) are intentional: PROMISE tables are the canonical state-machine record (used to reconstruct the agent on the next request), while `legacy_messages` is a denormalised read-friendly history for the frontend and analytics.

---

### Step 15 — Variant Switching (Optional, Anytime)

**What happens:** Mid-conversation, the visitor clicks a different variant button (e.g. switches from Variant 2 to Variant 1). The frontend creates a **new** PROMISE agent with the new variant's prompt, includes the old conversation as context, and loads the previous history for the new mode if any exists.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — `switchMode()` function: changes `currentMode`, updates `getScopedVisitorId(newMode)`, calls `createLegacyAgent()` again with the new prompt

**Backend / Railway:**
- Same flow as Step 12: a new `POST /agent/singlestate` request creates another agent on Railway.

**PROMISE adaptations:**
- The agent itself is stock PROMISE. The mode-scoped `visitor_id` is the Oblivio mechanism that keeps the three histories isolated per browser.

**Where the data goes:**
- Supabase `legacy_messages` filter changes (`WHERE visitor_id = '<uuid>__<newmode>'`)
- A new agent UUID is created in Supabase PROMISE-tables
- Old agent stays untouched (the visitor can return)

---

### Step 16 — Session Ends

**What happens:** The visitor closes the browser, or the persona's "goodbye" guard fires and transitions the agent to Final. Either way, the conversation history is persisted.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — `loadConversationHistory()` rehydrates the chat on the next visit

**Backend / Railway:**
- [`model/Final.java`](src/main/java/ch/zhaw/statefulconversation/model/Final.java) — Final state if goodbye triggered
- [`model/commons/actions/StaticExtractionAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticExtractionAction.java) — extracts a summary of the whole legacy chat to storage (optional analytics)

**PROMISE adaptations:** none — final-state handling is stock PROMISE.

**Where the data goes:**
- Everything stays in Supabase. On return, the frontend reads `legacy_messages` filtered by `access_code` and `visitor_id` (mode-scoped) and rebuilds the chat history exactly.
- Different devices show **different histories** because `visitor_id` is per-browser via `localStorage`.

---

## Recap: What PROMISE Provided vs What Oblivio Added

PROMISE provided the **state-machine framework** — the abstract concept of states, transitions, decisions, actions, plus the LLM glue and persistence via JPA/Hibernate. Oblivio used those building blocks to construct:

- **The Biographer** (21 states, 70 prompts in 8 languages) — [`AgentMetaUtility.createBiographerAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64)
- **The Legacy Chat** (one state + three variants) — same factory, different inputs
- **Context Compaction** — [`Utterances.compactIfNeeded()`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118)
- **Multi-user support** — `userId` field in [`Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) + [`UserLogController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java)
- **TTS bridge** — [`TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) + ElevenLabs
- **CORS for frontend separation** — [`WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java)
- **Live log streaming** — [`logging/`](src/main/java/ch/zhaw/statefulconversation/logging/) package (4 new classes)
- **PostgreSQL migration** — driver swap in [`pom.xml`](pom.xml) + TEXT columns + PgBouncer-compatible Hikari settings
- **Production deployment** — [`Dockerfile`](Dockerfile), [`railway.json`](railway.json), [`application-prod.properties`](src/main/resources/application-prod.properties), [`openai-prod.properties`](src/main/resources/openai-prod.properties)
- **The frontend** — 17 HTML pages, 12 JS files (template at [`Website-template/`](Website-template/))

In total: ~30 PROMISE files unchanged, 7 lightly modified, 3 heavily extended, 11 new Java files, plus complete frontend and infrastructure.

---

## Author

**Dennis Riccardo Dewiri**
Bachelor's Thesis · [ZHAW School of Management and Law](https://www.zhaw.ch/en/sml/) · Business Informatics · 2026

Built on top of the [PROMISE Framework](https://github.com/zhaw-iwi/promise) from the ZHAW Institute for Applied Information Technology.

---

<div align="center">
<sub>Built with conviction that every person deserves to be heard and remembered.</sub>
</div>
