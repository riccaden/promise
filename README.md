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

- **20 states (+1 end state)** in the Biographer (10 blocks × 2 + Final)
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

## Table of Contents

**Foundations**
- [What is Oblivio?](#what-is-oblivio)
- [Deep Dive: Inside the PROMISE Framework](#deep-dive-inside-the-promise-framework) — the 5 core primitives (Prompt, State, Transition, Decision, Action), Agent, Utterances, LMOpenAI, request lifecycle, persistence
- [Deep Dive: The 20-State (+1 End State) Biographer Architecture](#deep-dive-the-20-state-1-end-state-biographer-architecture) — state-machine layout, why 2 states per block, prompt components, backwards construction, token economics

**Setup**
- [Supabase Schema — Tables and Columns You Must Create](#supabase-schema--tables-and-columns-you-must-create) — full CREATE TABLE script for all 9 Oblivio tables + RLS policies + verification table

**End-to-End Process**
- [End-to-End Process — Overview](#end-to-end-process)

*Perspective 1 — The Persona Owner*
- [Step 1 — Sign Up / Log In](#step-1--sign-up--log-in)
- [Step 2 — Choose Language](#step-2--choose-language)
- [Step 3 — Block 0: Pre-Survey + GDPR Consent](#step-3--block-0-pre-survey--gdpr-consent)
- [Step 4 — Biographer Agent is Created](#step-4--biographer-agent-is-created)
- [Step 5 — The 10 Blocks (Conversation + Confirmation)](#step-5--the-10-blocks-conversation--confirmation)
  - [5a. Conversation Phase](#5a-conversation-phase)
  - [5b. Transition: Conversation → Confirmation](#5b-transition-conversation--confirmation)
  - [5c. Confirmation Phase](#5c-confirmation-phase)
- [Step 6 — All Blocks Complete: Final State + Access Code Generation](#step-6--all-blocks-complete-final-state--access-code-generation)
- [Step 7 — Persona Prompts Are Created (Manual Step)](#step-7--persona-prompts-are-created-manual-step)
- [Step 8 — Optional: Voice and Avatar Assignment](#step-8--optional-voice-and-avatar-assignment)

*Perspective 2 — The Visitor*
- [Step 9 — Open Legacy Chat with Access Code](#step-9--open-legacy-chat-with-access-code)
- [Step 10 — Enter Visitor Info (Name, Relation, Gender)](#step-10--enter-visitor-info-name-relation-gender)
- [Step 11 — Choose Conversation Variant](#step-11--choose-conversation-variant)
- [Step 12 — Build the System Prompt + Create Legacy Agent](#step-12--build-the-system-prompt--create-legacy-agent)
- [Step 13 — Starter Message](#step-13--starter-message)
- [Step 14 — The Conversation Loop](#step-14--the-conversation-loop)
- [Step 15 — Variant Switching (Optional, Anytime)](#step-15--variant-switching-optional-anytime)
- [Step 16 — Session Ends](#step-16--session-ends)

**PROMISE Adaptations** (what was changed in the upstream framework)
- [PROMISE Adaptations — Overview](#promise-adaptations-what-was-changed-and-why) — 15 detailed adaptations
- [Summary Table: All PROMISE Adaptations](#summary-table-all-promise-adaptations)
- [Appendix: PROMISE Adaptations at a Glance](#appendix-promise-adaptations-at-a-glance) — compact one-page reference grouped by concern

**Closing**
- [Recap: What PROMISE Provided vs What Oblivio Added](#recap-what-promise-provided-vs-what-oblivio-added)
- [Author](#author)

---

## Deep Dive: Inside the PROMISE Framework

Before we look at the 20-state Biographer or the Oblivio additions, this section explains **how PROMISE itself works internally** — what each class does, how they interact, and what happens technically during a conversation. Useful as a mental model before reading the rest.

### The five core primitives

PROMISE models a conversation as a graph of small, focused components. There are five primitive types you need to understand:

#### 1. `Prompt` — the base class

[`model/Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java) is the abstract base that everything inherits from. It holds **one string** — a prompt text — that gets sent to the LLM. Crucially, it uses JPA's **SINGLE_TABLE inheritance strategy** (`@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`) so that all subclasses — State, Decision, Action — are stored in **one single database table** called `prompt` with a `dtype` discriminator column telling them apart.

Why one table? Because PROMISE wants you to be able to mix and match these freely in a state machine without worrying about separate tables and foreign keys. They are conceptually "things that get sent to an LLM", just used in different ways.

#### 2. `State` — a conversation phase

[`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) extends `Prompt` and represents one phase of a conversation. Each state has:

- **A system prompt** (inherited from `Prompt`) — defines what the AI should do in this phase
- **A `starterPrompt`** — instructions for the AI's first message when this state is entered
- **A `summarisePrompt`** — used if `summarise()` is called on the state
- **A `Utterances` collection** — the messages exchanged while in this state
- **A list of `Transition` objects** — the possible ways out of this state
- **Two flags:** `isStarting` (should the AI generate a greeting when entering?), `isOblivious` (should utterances be reset when entering?)

The key methods on `State`:

```java
public Response start()                                      // generate the first message
public Response respond(String userSays) throws TransitionException  // handle user message
public void acknowledge(String userSays) throws TransitionException  // add user msg + check transitions
public String summarise()                                    // summarise this state's history
public void reset()                                          // clear utterances
```

#### 3. `Transition` — the bridge between states

[`model/Transition.java`](src/main/java/ch/zhaw/statefulconversation/model/Transition.java) is **not** a subclass of `Prompt`. It's a standalone entity that ties three things together:

```java
public class Transition {
    @ManyToMany List<Decision> decisions;     // Guards (boolean checks)
    @ManyToMany List<Action> actions;         // Side effects on transition
    @ManyToOne  State subsequentState;        // Where the transition leads
}
```

The two important methods:

```java
public boolean decide(Utterances utterances) {
    // Runs every decision in sequence; AND-logic
    // If any decision returns false → transition does NOT fire
    // If all decisions return true → transition fires
}

public void action(Utterances utterances) {
    // Runs every action in sequence (sequential, not parallel)
    // Each action can read/modify utterances or storage
}
```

#### 4. `Decision` — a boolean guard

[`model/Decision.java`](src/main/java/ch/zhaw/statefulconversation/model/Decision.java) extends `Prompt`. Its prompt is **a yes/no question**. The decision's role is to consult the LLM with this question against the current conversation, and return `true` or `false`.

PROMISE provides **[`StaticDecision`](src/main/java/ch/zhaw/statefulconversation/model/commons/decisions/StaticDecision.java)** as the implementation Oblivio uses — a fixed yes/no prompt that gets evaluated by the LLM.

The mechanism for evaluation: `LMOpenAI.decide()` sends the conversation + the decision's prompt + a strict instruction *"answer only true or false"* to GPT-4o, parses the answer with `Boolean.parseBoolean()`, and returns the result.

#### 5. `Action` — a side effect on transition

[`model/Action.java`](src/main/java/ch/zhaw/statefulconversation/model/Action.java) extends `Prompt`. When a transition fires, all its actions execute sequentially. Each action implements `execute(Utterances)`.

The actions that matter for Oblivio's Biographer:
- **[`StaticExtractionAction`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticExtractionAction.java)** — Sends the conversation + its prompt to the LLM via `LMOpenAI.extract()`, receives JSON, stores it in the agent's `Storage` under a given key (e.g., `"block1"`).
- **[`TransferUtterancesAction`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/TransferUtterancesAction.java)** — Copies the current state's utterances into the target state's utterances. Used in the Biographer to pass Conv-state messages into the Confirm-state so it can summarise them.
- **[`StaticSummarisationAction`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticSummarisationAction.java)** — Like extraction but for summarisation (less used in Oblivio).
- **[`RemoveLastUtteranceAction`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/RemoveLastUtteranceAction.java)** — Removes the last user message (used when re-asking after a misunderstood reply).

### The `Agent` — orchestrator

[`model/Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) is the top-level entity. It owns:

```java
@Id private UUID id;
private String name;
private String description;
private String userId;                  // Oblivio addition for multi-user
@OneToOne State initialState;          // Where conversations start
@OneToOne State currentState;          // Where the conversation currently is
@ManyToOne Storage storage;            // Key-value storage for extracted data
```

Key methods:

```java
public Response start()                                      // delegate to currentState.start()
public Response respond(String userSays)                     // delegate, handle TransitionException
public void reset()                                          // back to initialState
public boolean isActive()                                    // false if currentState is a Final
```

The clever part is `respond()`:

```java
public Response respond(String userSays) {
    try {
        return this.currentState.respond(userSays);
    } catch (TransitionException e) {
        this.currentState = e.getSubsequentState();    // Move to next state
        if (this.currentState.isStarting()) {
            return this.start();                       // New state generates a greeting
        }
        return this.respond(userSays);                 // Re-process the same message in new state
    }
}
```

If the response triggers a transition, the exception bubbles up. The agent **catches it, updates `currentState`, and re-runs `respond()` in the new state**. This means a single user message can cascade through multiple state transitions in one HTTP request — useful for skipping silent states (like the Confirm-state's transition to next-block Conv).

### `Utterances` — the conversation history

[`model/Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java) is the message log. Each utterance has:

```java
private String role;        // "user", "assistant", or "system"
private String content;     // the message text
private Instant createdDate;
private String stateName;   // which state this message belongs to
```

Crucially: **utterances are bound to a specific state's collection**. They don't automatically follow you across states. That's why `TransferUtterancesAction` exists — to explicitly copy them when needed.

PROMISE provides methods like `appendUserSays()`, `appendAssistantSays()`, `removeLastUtterance()`, `reset()`. Oblivio added **`compactIfNeeded()`** for context compaction (covered later).

### `LMOpenAI` — the LLM bridge

[`spi/LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) is a **static utility class** that wraps the OpenAI HTTP API. All LLM calls in PROMISE go through it. Its five public methods serve different purposes:

```java
// 1. Generate a normal assistant response (used by State.respond)
public static String complete(Utterances utt, String systemPrompt, String stateName)

// 2. Boolean decisions (used by Transition.decide via StaticDecision)
public static boolean decide(Utterances utt, String prompt)

// 3. Structured JSON extraction (used by StaticExtractionAction)
public static JsonElement extract(Utterances utt, String prompt)

// 4. JSON summarisation (used by StaticSummarisationAction)
public static JsonElement summarise(Utterances utt, String prompt)

// 5. Plain-text summarisation — added by Oblivio for context compaction
public static String summariseOffline(Utterances utt, String prompt)
```

Each method composes the prompt slightly differently:
- `complete()` puts the system prompt at the start, then the conversation
- `decide()` and `extract()` use a **condensed** form: the entire conversation is wrapped in `<conversation>...</conversation>` tags, treated as a single user message, and a reminder is appended ("Answer only true or false", "Return valid JSON")
- All methods use **GSON** to serialize messages and the **standard HTTP client** to call OpenAI

This abstraction means PROMISE doesn't have to know about OpenAI specifics — everything else in the framework works with Java strings and Java types.

### How a single HTTP request flows through PROMISE

Here is what happens when the frontend calls `POST /{agentId}/respond` with `{ "content": "I love cooking" }`:

```
1. Spring routes the request to AgentController.respond()
2. AgentController loads the agent from Supabase via AgentRepository (JPA)
   - This loads: Agent + currentState + currentState.utterances (EAGER fetch)
3. Spring deserializes the request body to UtteranceRequest
4. AgentController calls agent.respond("I love cooking")
   ↓
5. Agent.respond() delegates to currentState.respond("I love cooking")
   ↓
6. State.respond():
   a. acknowledge("I love cooking")
      - Appends utterance to utterances (with role="user")
      - Calls raiseIfTransit():
        ↓
        For each Transition in this.transitions:
          For each Decision in transition.decisions:
            - LMOpenAI.decide(utterances, decision.prompt) → HTTP POST to OpenAI
            - GPT-4o returns "true" or "false"
            - Boolean.parseBoolean()
          If all decisions returned true: 
            transition.action(utterances) → e.g., StaticExtractionAction
              ↓
              LMOpenAI.extract() → HTTP POST to OpenAI → JSON response
              storage.put("block1", json)
            throw TransitionException(subsequentState)
   
   b. (If no transition fired) compactIfNeeded()   ← Oblivio addition
      - If >20 user messages, summarise older ones via LMOpenAI.summariseOffline()
   
   c. composeTotalPrompt() — assembles the final system prompt
   
   d. LMOpenAI.complete(utterances, totalPrompt) → HTTP POST to OpenAI
      → GPT-4o returns the assistant's response text
   
   e. utterances.appendAssistantSays(text) — add response to history
   
   f. return new Response(this, text)
   ↓
7. Agent.respond() catches no exception, returns the Response
   (If a TransitionException was caught: currentState = subsequentState, recurse)
8. AgentController saves the modified agent back to Supabase:
   - repository.save(agent) → Hibernate persists agent + state + utterances + storage
9. AgentController returns a ResponseView (JSON) to the frontend
10. Frontend displays the assistant text
```

Notice how many HTTP calls to OpenAI happen for **one user message**: 1 per Decision (each Guard), 0 or 1 per Action (if it uses LMOpenAI), plus 1 final completion. A simple Biographer block with 2 decisions = 3 OpenAI calls per turn. That's why PROMISE is not "cheap" — it trades extra LLM calls for structural reliability.

### How persistence works (Hibernate / JPA)

PROMISE uses **Hibernate** with `spring.jpa.hibernate.ddl-auto=update`. This means:

- Every class with `@Entity` automatically becomes a database table
- Hibernate inspects the classes at startup and creates/extends tables as needed
- Foreign keys are built from `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany` relationships
- When `repository.save(agent)` is called, Hibernate **cascades** the save to all related entities (states, utterances, storage entries, transitions, decisions, actions)

The cascade is the magic. It means PROMISE never has to write SQL: just modify Java objects, call `save()`, and the entire object graph is persisted. The downside is that very large agents (lots of utterances) can be slow to load/save because Hibernate loads everything eagerly.

In the database, the entity hierarchy ends up as roughly these tables:

| Table | What it stores |
|---|---|
| `agent` | Each Agent: id, name, userId, currentState_id, initialState_id, storage_id |
| `prompt` | All States, Decisions, Actions, Finals (with `dtype` discriminator) |
| `state` | Inherited from Prompt; adds isStarting, isOblivious |
| `transition` | Each transition: id, subsequentState_id |
| `prompt_transitions` | Join table linking states to their transitions |
| `transition_decisions` | Join table linking transitions to their decisions |
| `transition_actions` | Join table linking transitions to their actions |
| `utterances` | Conversation containers |
| `utterance` | Individual messages: role, content, stateName, utterances_id |
| `storage` | Key-value containers attached to agents |
| `storage_entry` | The actual key-value pairs |

All of these live in **the same Supabase PostgreSQL database** as the Oblivio-specific tables (`user_agents`, `legacy_access_codes`, etc.).

### Summary of PROMISE's contribution

PROMISE provides:

1. **A clean state-machine abstraction** — states, transitions, decisions, actions, all as Java entities
2. **LLM integration** — five methods covering completion, decision, extraction, summarisation
3. **Persistence by convention** — JPA/Hibernate cascading; no SQL needed
4. **REST controllers** — `AgentController` and `AgentMetaController` ready to serve agents over HTTP
5. **Common building blocks** — pre-built actions and decisions in `model/commons/`

This is what Oblivio inherits **for free**. Everything else in the rest of this README is Oblivio sitting on top of this foundation.

---

## Deep Dive: The 20-State (+1 End State) Biographer Architecture

Before walking through the end-to-end process, let's understand the **core engineering decision** that makes Oblivio work: the **state-machine model of a conversation**.

### Why a state machine at all?

Imagine you're building a digital biographer with a single prompt: *"Ask the user about their childhood, then about their daily life, then about their values, then summarise everything."* This works for a minute — until the AI forgets where it is, skips topics, mixes themes, or "wraps up" too early.

PROMISE solves this by treating the interview as a **finite state machine (FSM)** — a graph of distinct conversation phases. Each phase has its own focused prompt, its own conversation history, and explicit rules for moving to the next phase. The AI never has to "remember everything at once"; it only needs to handle the current state.

> **A note on what PROMISE provides:** PROMISE was built from the start as a multi-state framework. Its primitives (`State`, `Transition`, `Decision`, `Action`, `Agent`) support arbitrary state-machine topologies — chains, branches, cycles, even nested machines. The test bots [`MultiStateInteraction.java`](src/test/java/ch/zhaw/statefulconversation/bots/MultiStateInteraction.java), [`MultiLayeredInteraction.java`](src/test/java/ch/zhaw/statefulconversation/bots/MultiLayeredInteraction.java), and [`TwoStatesInteraction.java`](src/test/java/ch/zhaw/statefulconversation/bots/TwoStatesInteraction.java) demonstrate that the multi-state capability was already there. **Oblivio did not have to add multi-state support — only the specific Biographer topology built on top of it** (which 20 states, what prompts in each, what guards, what actions). The state-machine engine itself is unchanged.

### The 20 states (+1 end state)

The Biographer chains together **20 states plus one end state** in a strictly linear sequence:

```
Block 1 Conv ──► Block 1 Confirm ──► Block 2 Conv ──► Block 2 Confirm ──► ... ──► Block 10 Conv ──► Block 10 Confirm ──► Final
   │ q1..q11      │ extract block1
   │ guard:       │ guard:
   │ "all asked?" │ "user confirmed?"
   ▼              ▼
```

That's 10 blocks × 2 states (Conversation + Confirmation) = **20 states**, plus one terminal **Final state** that signals "done" = **21 total**.

### What each state actually does

Every block has **two states**, paired:

| State Type | Purpose | Active Prompt | Transition Guard |
|---|---|---|---|
| **Conv state** | Run the questions for this block (3–11 questions depending on block) | Block-specific system prompt with question list + style rules | "Has the biographer asked all questions explicitly?" |
| **Confirm state** | Summarise what was learned, ask for confirmation | "Summarise the conversation, ask if the user confirms" | "Did the user confirm or accept the summary?" |

Plus one **Final state** at the end:
- Doesn't ask anything new; just signals "Biographer done"
- `isActive()` returns `false` → conversation is closed

### Why two states per block (and not one)?

Splitting Conversation and Confirmation is intentional:

- **Separation of concerns:** The Conv state focuses on *asking*. The Confirm state focuses on *summarising and validating*. Mixing both would dilute the AI's focus.
- **Data extraction happens only after explicit user consent:** The summary is shown to the user, and only after they confirm does a `StaticExtractionAction` create the structured JSON. No silent extraction behind the scenes.
- **Recovery:** If the user disagrees with the summary, the Confirm state stays active and the AI revises. The system never moves on with bad data.

### The 7 prompt components per block

Each block is fully defined by **7 prompt strings**, stored in a 2D array `prompts[10][7]`:

```java
String[][] prompts = new String[10][7];
prompts[blockIndex][0] = "Conv System Prompt";   // persona, questions, rules
prompts[blockIndex][1] = "Conv Starter Prompt";  // opening message
prompts[blockIndex][2] = "Conv Guard";           // "all questions asked?"
prompts[blockIndex][3] = "Confirm System Prompt";// "summarise, ask consent"
prompts[blockIndex][4] = "Confirm Starter";      // opening summary message
prompts[blockIndex][5] = "Confirm Guard";        // "user confirmed?"
prompts[blockIndex][6] = "Extract Prompt";       // JSON extraction template
```

That's **70 prompts in total** — 10 blocks × 7 components. Each in 8 languages would mean 560 strings, but Oblivio uses a clever trick: prompts are written in German, and a language prefix is prepended at runtime ([`getLanguageInstruction()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L480)) telling GPT-4o to translate everything to the target language. **Result: 70 strings instead of 560.**

### How transitions actually fire

After every user message, the current state's `respond()` method runs this sequence:

```
1. Append user message to utterances
2. Call compactIfNeeded() (Oblivio addition — token cost optimization)
3. For each transition out of this state:
     a. For each Decision (Guard) in the transition:
          - Send conversation + Guard prompt to GPT-4o
          - Parse response as boolean (true/false)
     b. If ALL guards return true: fire transition
4. If a transition fires:
     a. Run all Actions on the transition (sequentially)
     b. Throw TransitionException
     c. Caller (Agent.respond) catches it, sets currentState = subsequentState
5. Otherwise: generate normal assistant response via LMOpenAI.complete()
```

The Guard is an LLM-evaluated boolean question. PROMISE asks GPT-4o for a `true`/`false` answer, parses it with Java's `Boolean.parseBoolean()`, and decides accordingly. The two Actions used in the Biographer:

- **[`TransferUtterancesAction`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/TransferUtterancesAction.java)** — fires on Conv → Confirm transition. Copies the conversation messages into the Confirm state so it can summarise them.
- **[`StaticExtractionAction`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticExtractionAction.java)** — fires on Confirm → next-block-Conv transition. Calls GPT-4o with an extraction prompt, gets back structured JSON, and stores it in the agent's `Storage` under the key `block1` / `block2` / ... / `block10`.

### Why the chain is built backwards

This is one of the most counterintuitive parts of the code. Look at the factory method:

```java
State current = new Final("Biografie abgeschlossen", ...);

for (int i = 9; i >= 0; i--) {   // ← descending loop
    State nextState = current;

    // Build Confirm state first, with transition pointing to nextState
    Decision confirmGuard = new StaticDecision(prompts[i][5]);
    Action extract = new StaticExtractionAction(prompts[i][6], storage, "block" + (i + 1));
    Transition confirmTransition = new Transition(confirmGuard, extract, nextState);
    State confirmState = new State(prompts[i][3], ..., confirmTransition);

    // Build Conv state, with transition pointing to the Confirm state we just built
    Decision convGuard = new StaticDecision(prompts[i][2]);
    Action transfer = new TransferUtterancesAction(confirmState);
    Transition convTransition = new Transition(convGuard, transfer, confirmState);
    State convState = new State(prompts[i][0], ..., convTransition);

    current = convState;
}
```

**Why backwards?** Because in PROMISE, every `Transition` is constructed with a reference to its `subsequentState`. The successor state must exist *before* you can build the transition that points to it. If we tried to build Block 1 first, we'd have no Block 2 state to point to yet.

By starting at the end (`Final`) and building toward the start (`Block 1 Conv`), every newly created state already has its successor in hand. After the loop, the variable `current` holds **Block 1 Conv** — the entry point of the entire chain. That gets passed to the `Agent` constructor as `initialState`, and the agent is ready to start.

### Token economics of this design

A naive single-prompt biographer with 100 messages would send all 100 messages to GPT-4o on every new turn — token costs explode. The 20-state design (plus the Final state) saves money in three ways:

1. **Per-state utterances:** Each state has its own `Utterances` collection. When the user moves from Block 1 to Block 2, Block 1's conversation history is **not carried over**. Block 2 starts fresh, with only Block 1's *summary* (the extracted JSON) preserved.
2. **Compact summaries instead of full history:** The block summaries in `Storage` are short JSON objects (~500 chars) rather than 50-message conversation logs (~5000 chars).
3. **Context compaction** (Oblivio addition): even within a single block, after 20 user messages the older parts are summarised. See Step 5 below.

For a full 10-block interview, total token cost ends up roughly **8–12× lower** than a naïve single-prompt approach with the same content depth.

### State-machine diagram

Putting it all together visually:

```
                   ┌─────────────────────┐
            START  │  Block 1 Conv       │ ── q1..q11
                   │  utterances: []     │
                   └─────────┬───────────┘
                             │ Guard: "all questions asked?"
                             │ Action: TransferUtterancesAction
                             ▼
                   ┌─────────────────────┐
                   │  Block 1 Confirm    │ ── summarise + ask consent
                   │  utterances: copy   │
                   └─────────┬───────────┘
                             │ Guard: "user confirmed?"
                             │ Action: StaticExtractionAction
                             │         → storage[block1] = JSON
                             ▼
                   ┌─────────────────────┐
                   │  Block 2 Conv       │ ── fresh utterances
                   │  utterances: []     │
                   └─────────┬───────────┘
                             │
                             ▼
                          ... 8 more blocks ...
                             │
                             ▼
                   ┌─────────────────────┐
                   │  Block 10 Confirm   │
                   └─────────┬───────────┘
                             │ Guard: "user confirmed?"
                             │ Action: StaticExtractionAction
                             ▼
                   ┌─────────────────────┐
                   │  Final              │ ── isActive() = false
                   │  Storage: 10 blocks │ ── ready for retrieval
                   └─────────────────────┘
```

After the Final state is reached, the frontend fetches `GET /{agentId}/storage` and gets all 10 block JSONs as one response.

---

## Supabase Schema — Tables and Columns You Must Create

**Read this section first if you are rebuilding Oblivio from scratch.** Every "Where the data goes" line later in this README points to one of the tables defined here. Without these tables (and the columns shown), the frontend's `.insert()` and `.select()` calls will fail silently and the conversation will not persist.

There are **two groups of tables** in the Supabase project:

- **Group A — Created automatically by Hibernate** (PROMISE-managed). The Spring Boot backend runs with `spring.jpa.hibernate.ddl-auto=update`, so the first request after deploy creates these. You do **not** write SQL for them. Tables: `agent`, `state`, `prompt`, `transition`, `prompt_transitions`, `utterance`, `utterances`, `storage`, `storage_entry`.
- **Group B — Created manually by you** (Oblivio-specific). These are written directly from the browser via the Supabase JS client. You must create them with SQL in the Supabase SQL Editor *before* the website can save anything. The complete script lives at [`sql/SUPABASE_TABLES.sql`](sql/SUPABASE_TABLES.sql) (core) and [`sql/supabase_migrations.sql`](sql/supabase_migrations.sql) (with share-token extension). The 9 tables below are what the frontend in [`Website-template/`](Website-template/) expects.

### How "field on the website → column in Supabase" works (mental model)

The pattern is always the same. There is no ORM, no API layer, no glue code — the frontend writes columns by name.

```javascript
//  1. A user types something into an HTML form field:
//     <input id="nickname-input" />
const nickname = document.getElementById('nickname-input').value;

//  2. JavaScript builds a plain object whose KEYS MUST MATCH the Supabase column names:
const row = {
    user_id: currentUser.id,    // ← column 'user_id'   on table 'user_profiles'
    nickname: nickname,          // ← column 'nickname'  on table 'user_profiles'
    completed_at: new Date().toISOString()
};

//  3. One Supabase call writes the row. The library translates the object
//     literally — keys become column names, values become column values.
await supabaseClient.from('user_profiles').upsert(row, { onConflict: 'user_id' });
```

**To add a new field that flows from website → Supabase, you do three things:**

1. **Add the column in Supabase** (SQL Editor → `ALTER TABLE user_profiles ADD COLUMN birthplace TEXT;`)
2. **Capture the value in the browser** (`<input id="birthplace">` + `document.getElementById('birthplace').value`)
3. **Add the key to the JS object** that gets inserted (`row.birthplace = ...`) — the *key name must equal the column name*.

That's it. Supabase's PostgREST layer auto-exposes every column as part of the row; no migration of code is needed beyond those three points.

If you forget step 1, the insert fails with `column "birthplace" of relation "user_profiles" does not exist`. If you forget step 3, the column stays NULL forever.

### The 9 Oblivio-specific tables

| Table | What it stores | Written by | Read by |
|---|---|---|---|
| `user_profiles` | Per-user nickname (the Biographer's name for them) | [`biographer.html`](Website-template/biographer.html) `saveUserProfile()` | [`biographer.html`](Website-template/biographer.html), [`journey.html`](Website-template/journey.html) |
| `user_consents` | GDPR consent flag per user | [`biographer.html`](Website-template/biographer.html) `saveUserConsent()` | [`biographer.html`](Website-template/biographer.html) `checkUserConsent()` |
| `questionnaire_answers` | All Pre-Survey (Block 0) answers as one JSONB | [`biographer.html`](Website-template/biographer.html) `saveUserProfile()` | Admin / analytics only |
| `user_agents` | Maps a Supabase user → a PROMISE agent UUID (per language) | [`biographer-promise.js`](Website-template/js/biographer-promise.js) `getOrCreatePromiseAgent()` | Same file (resume logic) |
| `chat_messages` | Denormalised chat history of the Biographer interview | [`biographer-promise.js`](Website-template/js/biographer-promise.js) `saveChatMessage()` | `loadChatMessages()` (resume) |
| `biographer_conversations` | Per-block log of every Biographer message (with block number + state) | [`biographer-promise.js`](Website-template/js/biographer-promise.js) | Admin / analytics |
| `user_legacies` | Final 10 block summaries as one JSONB (the "biography") | [`biographer-promise.js`](Website-template/js/biographer-promise.js) `saveLegacyToSupabase()` | [`legacy.html`](Website-template/legacy.html) (fallback path) |
| `legacy_access_codes` | The 8-char share code + persona prompts + voice + avatar | [`biographer.html`](Website-template/biographer.html) (insert), admin SQL (prompt update) | [`legacy.html`](Website-template/legacy.html), [`journey.html`](Website-template/journey.html) |
| `legacy_messages` | Every visitor↔persona message (one row per turn, scoped by visitor_id + mode) | [`legacy.html`](Website-template/legacy.html) `saveMessage()` | [`legacy.html`](Website-template/legacy.html) `loadConversationHistory()` |

Plus `auth.users` — managed entirely by Supabase Auth, you do not create or modify it.

### Complete CREATE TABLE script

Paste this into the **Supabase SQL Editor** once, on a fresh project. It is idempotent (uses `CREATE TABLE IF NOT EXISTS`), so it is safe to re-run.

```sql
-- ============================================
-- 1. user_profiles — nickname per Supabase user
-- ============================================
CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    nickname TEXT,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 2. user_consents — GDPR consent
-- ============================================
CREATE TABLE IF NOT EXISTS user_consents (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    consented BOOLEAN NOT NULL,
    consented_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 3. questionnaire_answers — full Pre-Survey responses
-- ============================================
CREATE TABLE IF NOT EXISTS questionnaire_answers (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    answers JSONB NOT NULL,
    completed_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 4. user_agents — user → PROMISE agent mapping (per language)
-- ============================================
CREATE TABLE IF NOT EXISTS user_agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    nickname TEXT,                                   -- so a name change starts a fresh agent
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, language)
);

-- ============================================
-- 5. chat_messages — flat Biographer chat history (for resume)
-- ============================================
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('user','assistant')),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 6. biographer_conversations — per-block log (with block number + state)
-- ============================================
CREATE TABLE IF NOT EXISTS biographer_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    block_number INTEGER,
    state_name TEXT,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 7. user_legacies — final 10 block summaries (the "biography")
-- ============================================
CREATE TABLE IF NOT EXISTS user_legacies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    legacy_data JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

-- ============================================
-- 8. legacy_access_codes — the 8-char share code + persona prompts
-- ============================================
CREATE TABLE IF NOT EXISTS legacy_access_codes (
    access_code VARCHAR(8) PRIMARY KEY,             -- e.g. 'VDSRMACZ'
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    nickname TEXT,
    language VARCHAR(10),
    legacy_data JSONB,                              -- contains full_prompt_active / _passive / _analysis
    voice_id TEXT,                                  -- ElevenLabs voice identifier (optional)
    avatar_url TEXT,                                -- relative path to avatar image (optional)
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 9. legacy_messages — visitor ↔ persona chat history
-- ============================================
CREATE TABLE IF NOT EXISTS legacy_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    access_code VARCHAR(8) NOT NULL REFERENCES legacy_access_codes(access_code) ON DELETE CASCADE,
    visitor_id TEXT NOT NULL,                       -- mode-scoped: '<uuid>__active' / '__passive' / '__analysis'
    visitor_name TEXT,
    user_id UUID,                                   -- the persona owner (nullable for guests)
    role TEXT NOT NULL CHECK (role IN ('user','legacy')),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- Performance indexes
-- ============================================
CREATE INDEX IF NOT EXISTS idx_user_agents_user_id        ON user_agents(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_user_agent   ON chat_messages(user_id, agent_id);
CREATE INDEX IF NOT EXISTS idx_user_legacies_user_id      ON user_legacies(user_id);
CREATE INDEX IF NOT EXISTS idx_legacy_access_codes_user   ON legacy_access_codes(user_id);
CREATE INDEX IF NOT EXISTS idx_legacy_messages_code_vis   ON legacy_messages(access_code, visitor_id);
```

### Row-Level Security (RLS) — critical for production

Without RLS, **any** anonymous request can read **any** row. With RLS turned on plus the policies below, a logged-in user can only see their own rows, and visitors with an access code can only read the public-by-design columns of the persona they're talking to.

```sql
-- Turn RLS on
ALTER TABLE user_profiles          ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_consents          ENABLE ROW LEVEL SECURITY;
ALTER TABLE questionnaire_answers  ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_agents            ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_messages          ENABLE ROW LEVEL SECURITY;
ALTER TABLE biographer_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_legacies          ENABLE ROW LEVEL SECURITY;
ALTER TABLE legacy_access_codes    ENABLE ROW LEVEL SECURITY;
ALTER TABLE legacy_messages        ENABLE ROW LEVEL SECURITY;

-- A user can read / write only their own rows on the "owner" tables
CREATE POLICY "own_rows" ON user_profiles         FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own_rows" ON user_consents         FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own_rows" ON questionnaire_answers FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own_rows" ON user_agents           FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own_rows" ON chat_messages         FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own_rows" ON biographer_conversations FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own_rows" ON user_legacies         FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

-- legacy_access_codes: owner manages, anyone can read an active code (visitors don't log in)
CREATE POLICY "owner_manage" ON legacy_access_codes FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "public_read_active" ON legacy_access_codes FOR SELECT USING (is_active = true);

-- legacy_messages: anyone can read/write rows for an active code (visitors are anonymous)
CREATE POLICY "active_code_messages" ON legacy_messages FOR ALL
    USING ( EXISTS (SELECT 1 FROM legacy_access_codes c WHERE c.access_code = legacy_messages.access_code AND c.is_active = true) )
    WITH CHECK ( EXISTS (SELECT 1 FROM legacy_access_codes c WHERE c.access_code = legacy_messages.access_code AND c.is_active = true) );
```

### How to verify the schema is wired correctly

After running the SQL above, open the website locally and check the Supabase **Table Editor** in the browser as you go:

| You do this on the website | Then this row appears |
|---|---|
| Sign up | `auth.users` (automatic, by Supabase) |
| Accept GDPR modal in Biographer | `user_consents` |
| Finish Block 0 (Pre-Survey) | `user_profiles` (nickname) + `questionnaire_answers` (full JSONB) |
| First Biographer message | `user_agents` (one row), then `chat_messages` grows with every turn |
| Finish Block 10 | `user_legacies` (final JSONB) + `legacy_access_codes` (your 8-char code) |
| Open the access code in Legacy Chat | (no row yet, just a SELECT on `legacy_access_codes`) |
| Send a message to the persona | `legacy_messages` grows |

If a row does not appear, the browser console will show a Supabase error like `relation "user_profiles" does not exist` (you forgot the CREATE) or `new row violates row-level security policy` (RLS is on but no policy matches — usually because the user is not logged in or the `user_id` value doesn't match `auth.uid()`).

---

## End-to-End Process

The full journey from "user registers" to "loved ones chat with the digital persona", in **two perspectives**:

- **Perspective 1 — The Persona Owner:** Someone who registers on oblivio.ch, takes the Biographer interview, and generates a digital persona of themselves
- **Perspective 2 — The Visitor:** A loved one who receives an access code and chats with that persona

---

## Perspective 1: The Persona Owner

### Step 1 — Sign Up / Log In

**What happens:** User opens oblivio.ch, clicks Sign Up, enters email and password, verifies via email, then logs in.

**Frontend files in detail:**

| File | What it specifically does | Why it's designed this way |
|---|---|---|
| [`Website-template/signup.html`](Website-template/signup.html) | Renders the signup form (email + password + password confirmation fields). Inline JavaScript at the bottom of the file initialises the Supabase JS client, hooks the form's `submit` event, and calls `supabase.auth.signUp({ email, password })`. On success, it redirects to `login.html`. On failure (e.g. duplicate email), shows an error toast. | **Direct Supabase call (no Oblivio backend in between)** because Supabase Auth is a complete service — passing it through Railway would just add latency and a security risk (we'd have to handle passwords ourselves). The inline JS keeps each HTML page self-contained, so there's no build step needed. |
| [`Website-template/login.html`](Website-template/login.html) | Renders the login form. Inline JavaScript calls `supabase.auth.signInWithPassword({ email, password })`. On success, Supabase returns a JWT which is automatically stored in the browser by the JS client; the user is redirected to `journey.html` (the dashboard). | **The JWT auto-storage is critical:** every subsequent Supabase call on every page is automatically authenticated without any custom token-handling code. Supabase's JS client refreshes the token transparently before expiry. This is why all Oblivio pages just call `supabase.from('table').select()` — auth is invisible to the page logic. |

**Backend / Railway:**
- Not involved in this step. Supabase Auth handles registration entirely on its own.

**PROMISE adaptations:**
- None — authentication is handled by Supabase, not by PROMISE. PROMISE had no auth system; Oblivio relies entirely on Supabase Auth.

**Where the data goes — Supabase fields written in this step:**

| Table | Column | Source on the website | Why this column |
|---|---|---|---|
| `auth.users` | `email`, `encrypted_password`, `email_confirmed_at` | The two form fields in [`signup.html`](Website-template/signup.html) (`<input type="email">`, `<input type="password">`) — handed to `supabase.auth.signUp({ email, password })` | Managed entirely by Supabase Auth. You don't create this table or insert into it manually. |

No Oblivio-specific table is touched yet — that starts in Step 3 (consent) and Step 4 (agent creation). The user's UUID (`auth.users.id`) is what every later table joins on.

**To add a new signup field** (e.g. "preferred name" at signup) you would: (1) add `<input id="preferred-name">` to signup.html, (2) collect it in the submit handler, (3) call `supabase.from('user_profiles').upsert({ user_id: data.user.id, preferred_name: ... })` immediately after `signUp()` returns — Step 1's `auth.users` row cannot hold custom columns directly.

---

### Step 2 — Choose Language

**What happens:** User picks one of 8 languages from a dropdown. The whole UI switches plus the Biographer interview is conducted in that language.

**Frontend files in detail:**

| File | What it specifically does | Why it's designed this way |
|---|---|---|
| [`Website-template/biographer.html`](Website-template/biographer.html) | Contains the language-picker dropdown near the top, plus all UI elements tagged with `data-i18n="key"` attributes that get filled by translations.js. Saves the user's choice to `localStorage('oblivio_language')` and triggers a full UI refresh. | **The `data-i18n` attribute pattern** means designers can write static HTML with English placeholders and i18n is added later without touching the markup — no template engine, no build step. localStorage is used (not a cookie) because language is a per-device preference, not a per-account one (e.g., one user may want German on phone but English on desktop). |
| [`Website-template/js/translations.js`](Website-template/js/translations.js) | The i18n engine. On page load, it reads `localStorage('oblivio_language')`, loads the corresponding `lang-<code>.js` file, then walks the DOM and replaces every `[data-i18n="key"]` element's text with the matching translation. Also re-runs on language change and observes the DOM for dynamically added elements. | **DOM walking + MutationObserver** instead of a virtual DOM library: keeps the bundle tiny (vanilla JS, no framework dependency). The observer catches translations on dynamically inserted elements like chat messages and modals — important because the chat UI continuously creates new DOM nodes during conversation. |
| [`Website-template/js/lang-de.js`](Website-template/js/lang-de.js) ... [`lang-zh.js`](Website-template/js/lang-zh.js) | One file per language. Each is a JavaScript object with ~400 key-value pairs: `nav_home: 'Startseite'`, `biographer_welcome: '...'`, etc. They all set `window.OBLIVIO_TRANSLATIONS[<code>] = { ... }` so translations.js can pick them up. | **Separate file per language** so only the active language gets downloaded (saves ~50 KB per page load vs bundling all 8). Plus each file can be edited independently — fixing a typo in Turkish doesn't risk breaking German. Using plain JS objects (not JSON files) means no fetch + parse step; the file is loaded as a script and instantly available. |

**Backend / Railway:**
- Not yet — language is recorded locally first, then passed to backend in Step 4.

**PROMISE adaptations — what was changed and why it works now:**
- **Why it was needed:** PROMISE was English-only. It has no concept of "select a target language for this agent". Without changes, every Biographer interview would default to whatever language is hard-coded in the prompts.
- **What was changed:** A new method [`getLanguageInstruction()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L480) was added in `AgentMetaUtility.java`. The factory accepts a `language` parameter from the DTO. For each of the 70 block prompts, the language instruction is prepended at build time.
- **How it works now:** The prompts themselves stay written in **German** (one set, 70 strings). At runtime, the language prefix tells GPT-4o: *"The following instructions are in German, but you MUST communicate exclusively in Korean. Translate all questions and answers to Korean."* GPT-4o handles the translation on the fly. This saves us from maintaining 8 × 70 = 560 strings; we only need 70 + 8 (one prefix per language).
- **Trade-off:** Slightly higher tokens per response (the prefix is ~80 tokens) in exchange for one canonical source of truth. Translation quality is excellent because GPT-4o is multilingual-native.

**Where the data goes:**
- `localStorage('oblivio_language')` — kept on the user's device only

---

### Step 3 — Block 0: Pre-Survey + GDPR Consent

**What happens:** Before the actual Biographer starts, the user (1) accepts a GDPR consent modal and (2) fills out a short questionnaire: nickname, age range, gender, personality traits, communication style. This gives the Biographer agent context for personalisation.

**What this step needs to work:**
- User must be logged in (Step 1 must have happened — we need `currentUser.id` from `supabase.auth.getUser()`).
- Tables `user_consents`, `user_profiles`, and `questionnaire_answers` must exist in Supabase (see the Schema section above). If they don't, the inserts fail silently and the user can still continue, but no data persists.
- RLS policies on these tables must accept `auth.uid() = user_id` — otherwise inserts return `42501 new row violates row-level security policy`.

**Frontend files in detail:**

| File | What it specifically does | Why it's designed this way |
|---|---|---|
| [`Website-template/biographer.html`](Website-template/biographer.html) | Three things in one page: the consent modal (rendered on first visit), the Pre-Survey form (rendered after consent), then the chat UI (rendered after Pre-Survey). `checkUserConsent()` / `saveUserConsent()` write the boolean. `saveUserProfile()` writes the nickname to `user_profiles` AND the full answer set to `questionnaire_answers` in two separate calls. | **Two writes for the questionnaire** (nickname → `user_profiles`, full JSON → `questionnaire_answers`) because the nickname is needed at every page load (dashboard, biographer, journey) and a small text column is much faster to read than parsing a JSONB blob. The JSONB copy is kept for analytics and to later re-derive personality hints. |

**Backend / Railway:**
- Not involved. The frontend writes directly to Supabase.

**PROMISE adaptations:**
- None — this is purely an Oblivio addition that happens before any PROMISE agent is created.

**Where the data goes — Supabase fields written in this step:**

| Table | Column | Source on the website | Required? |
|---|---|---|---|
| `user_consents` | `user_id` | `currentUser.id` from Supabase Auth | yes |
| `user_consents` | `consented` | The "Ja" / "Nein" button in the consent modal | yes |
| `user_consents` | `consented_at` | `new Date().toISOString()` at click time | yes |
| `user_profiles` | `user_id` | `currentUser.id` | yes |
| `user_profiles` | `nickname` | `<input id="nickname-input">` value | yes — used as `{{nickname}}` in all Biographer prompts |
| `user_profiles` | `completed_at` | timestamp when the Pre-Survey is finished | optional |
| `questionnaire_answers` | `user_id` | `currentUser.id` | yes |
| `questionnaire_answers` | `answers` | The full `profileData` object (age, gender, traits…) as JSONB | yes if you want analytics |
| `questionnaire_answers` | `completed_at` | timestamp | optional |

**How to add a new Pre-Survey question that flows to Supabase:**
1. Add the `<input>` / `<select>` to the form section in `biographer.html` (around the "Block 0" UI). Give it a unique `id`.
2. In `collectProfileData()` (same file), add a line `profileData.your_field = document.getElementById('your-id').value;`. Because `answers` is JSONB, **you don't need to change Supabase at all** — the new key is just stored inside the existing JSONB column.
3. If you also want the field as its own queryable column, run `ALTER TABLE user_profiles ADD COLUMN your_field TEXT;` and add the key to the `user_profiles.upsert(...)` call in `saveUserProfile()`.

---

### Step 4 — Biographer Agent is Created

**What happens:** Frontend sends a POST request to the backend asking for a new Biographer. The backend builds a 20-state agent + 1 end state (10 blocks × 2 + Final) on the fly and returns its ID.

**What this step needs to work:**
- User logged in + Steps 1–3 done (we need `user_profiles.nickname` for the prompts).
- Railway backend reachable at the URL set in `js/config.js` → `PROMISE_API_URL` (e.g. `https://promise-production.up.railway.app`).
- CORS must allow your frontend origin — see [`WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java). If you self-host, add your domain to the `allowedOrigins` list or the browser blocks the POST with a CORS error.
- Supabase environment variables on Railway: `DB_URL`, `DB_USER`, `DB_PASS` pointing at the Supabase Postgres connection. Plus `OPENAI_API_KEY` for the LLM calls that fire inside the agent. Without these the backend starts but `repository.save(agent)` fails on the first call.
- Table `user_agents` must exist in Supabase (frontend writes to it).

**What PROMISE does in this step (concretely):**

This is the step where the PROMISE framework does the most work. Everything below runs inside [`AgentMetaUtility.createBiographerAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64):

1. **Builds 21 `State` objects in reverse** — Final → Block 10 Confirm → Block 10 Conv → Block 9 Confirm → … → Block 1 Conv. Reverse order is required because PROMISE's `Transition` constructor takes its `subsequentState` as an argument, so the target must exist before the source.
2. **Loads all 70 prompts** via `buildBlockPrompts(language, nickname)` — a `String[10][7]` array of system / starter / summarise / guard / extract / etc. text for every block. Each prompt is wrapped in a `Prompt` entity that JPA will later persist as a row in the `prompt` table.
3. **Wires every `Transition`** — Conv → Confirm gets `TransferUtterancesAction` (copies the chat into the Confirm state) and a `StaticDecision` Guard ("have all questions been asked?"). Confirm → next-block-Conv gets `StaticExtractionAction` (writes the block summary to `Storage`) plus its own Guard ("did the user confirm?").
4. **Creates one shared `Storage` object** — the persistent key-value store where `block1`, `block2`, …, `block10` JSON summaries will live. All 20 states share the same `Storage` so any block can read what earlier blocks produced.
5. **Sets `agent.userId` from the DTO** — Oblivio's only intrusion into PROMISE's domain model (Adaptation 3). This is what later lets the journey dashboard filter "show me only my agents".
6. **Calls `agent.start()`** — pushes the agent into `Block 1 Conv` and generates its opening message via `LMOpenAI.complete()` using Block 1's starter prompt. The opening message is stored as the first row in `utterance`.
7. **Returns** an `AgentInfoView` containing the new UUID. PROMISE's role is complete; from here on the agent lives in Supabase and is reloaded on every `/respond` call.

Without PROMISE, you would have to hand-write the equivalent of these 21 states plus the transition logic plus a JPA persistence layer — roughly the 2000+ LOC that PROMISE provides for free.

**Frontend files in detail:**

| File | What it specifically does | Why it's designed this way |
|---|---|---|
| [`Website-template/biographer.html`](Website-template/biographer.html) | The Biographer UI: progress bar, chat area, input box. After Block 0 completes, it triggers `createBiographer()` from biographer-promise.js. Once the agent ID comes back, it stores it in `localStorage` (so the user can continue later) and writes the link to Supabase `user_agents`. | **localStorage + Supabase double-tracking** because the agent UUID must survive a refresh (so the user can return mid-interview) AND be queryable from any device (so the dashboard works). localStorage is fast for the current device; the Supabase row is the cross-device source of truth. |
| [`Website-template/js/biographer-promise.js`](Website-template/js/biographer-promise.js) | The PROMISE API client. Contains `createBiographer(nickname, language, userId)` which `fetch()`-POSTs to `https://promise-production.up.railway.app/agent/biographer` with a JSON body, and helpers like `sendMessage()`, `getState()`, `getStorage()`. Centralises every backend HTTP call so the UI doesn't have URLs and fetch logic scattered around. | **One central client module** so when the backend URL changes (it has changed at least once during the project), only one file needs an edit. Also makes it easy to add cross-cutting concerns like retry logic for Railway's cold starts (Railway containers spin down after inactivity and take ~5–10 seconds to wake up on the next request). |

**Backend / Railway files in detail:**

| File | What it specifically does | Why it's designed this way |
|---|---|---|
| [`controllers/AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) | Spring `@RestController`. Exposes `POST /agent/biographer` (added by Oblivio) and `POST /agent/singlestate` (original PROMISE). The biographer handler validates the DTO type, calls the factory, persists the resulting agent via `repository.save()`, and returns an `AgentInfoView` with the new UUID. | **Separate endpoint per agent type** instead of one overloaded endpoint with optional fields. This keeps the API explicit and self-documenting — and lets Spring's `@RequestBody` parser cleanly validate the right DTO without runtime type-checks. |
| [`controllers/AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java) | The factory. Its [`createBiographerAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64) method builds all 21 states backwards (Final → Block 10 → … → Block 1), wires every transition with its Guard and Action, creates the `Storage` object, sets `agent.userId`, and calls `agent.start()`. Plus the helper [`buildBlockPrompts()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L143) returns the 70-prompt 2D array, and [`getLanguageInstruction()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L480) returns the language prefix. | **Backwards construction** is structurally required: PROMISE's `Transition` constructor needs the `subsequentState` to exist already. **The factory pattern** centralises the wiring so creating a new Biographer for each user is one call (~50 LOC vs ~200 LOC inline). |
| [`controllers/dto/BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) | Java class that maps the JSON request body. Extends `SingleStateAgentCreateDTO`, adds `language` (e.g. "de") and `nickname` (e.g. "Maria") fields. Spring deserialises the incoming JSON into this object automatically. | **Extending the single-state DTO** instead of duplicating fields keeps the API consistent: shared fields like `agentName` and `userId` are defined in one place. If the base DTO ever needs a new field, the Biographer DTO inherits it automatically. |
| [`model/Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) | The created Agent object lives here. Constructor sets `initialState = currentState = Block 1 Conv`, plus the Oblivio-added `userId`. | **`initialState = currentState` at construction** because a fresh agent has not yet started — it's "parked" at the entry state. The two diverge as soon as the first transition fires. Keeping both as separate fields allows `agent.reset()` to return to the entry point at any time. |
| [`repositories/AgentRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/AgentRepository.java) | The Spring Data JPA repository. When the controller calls `repository.save(agent)`, Hibernate cascades through all 21 states, all transitions, decisions, actions, plus the storage — inserting ~50 database rows in one transaction. | **Cascade-save in one transaction** means there's never a half-saved agent in the database — either the whole structure is persisted or nothing is. If Railway crashes mid-save, the transaction rolls back and the next attempt starts clean. This safety is exactly why we accept the overhead of a 50-row insert. |

> **Important clarification:** PROMISE was designed for multi-state agents from day one — its whole purpose is to model conversations as state machines. The framework primitives (State, Transition, Decision, Action, Agent) already support arbitrary chains of states. Test files like [`MultiStateInteraction.java`](src/test/java/ch/zhaw/statefulconversation/bots/MultiStateInteraction.java) and [`MultiLayeredInteraction.java`](src/test/java/ch/zhaw/statefulconversation/bots/MultiLayeredInteraction.java) in the PROMISE repo prove this. **What Oblivio added is not the multi-state capability itself, but a specific application of it** — the Biographer is one concrete use case with one specific wiring of those primitives.

**PROMISE adaptations — what was changed and why it works now:**

- **New endpoint `POST /agent/biographer`:**
  - **Why needed:** PROMISE shipped with `POST /agent/singlestate` as one ready-made factory endpoint. There was no ready-made factory endpoint for a Biographer-shaped agent. A generic "build any state machine" endpoint would be possible, but a dedicated Biographer endpoint is cleaner because the inputs (language, nickname) and outputs (always 10 themed blocks) are very specific.
  - **What was changed:** Added a new `@PostMapping` handler in `AgentMetaController.java` that accepts the Biographer DTO, validates the type, and delegates to the new factory.
  - **How it works now:** The frontend explicitly calls a separate endpoint when it wants a Biographer — no overloading, no runtime type-checks. Clear API contract. PROMISE's multi-state framework does all the heavy lifting under the hood.

- **New enum value `biographer = 1`:**
  - **Why needed:** `AgentMetaType` had only `singleState = 0`. Without a new constant, the endpoint couldn't validate "is this really a Biographer request?".
  - **What was changed:** One line added to [`AgentMetaType.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaType.java).
  - **How it works now:** The DTO carries `type = 1`, the controller checks `AgentMetaType.biographer.getValue() == data.getType()` and rejects otherwise.

- **New DTO `BiographerAgentCreateDTO`:**
  - **Why needed:** `SingleStateAgentCreateDTO` doesn't have `language` or `nickname` fields. Forcing them in there would pollute the single-state API.
  - **What was changed:** Created a new class extending `SingleStateAgentCreateDTO` with two extra fields.
  - **How it works now:** Frontend sends `{ type: 1, language: 'de', nickname: 'Maria', ... }` — the backend maps it cleanly to a domain object.

- **New factory method `createBiographerAgent()`:**
  - **Why needed:** Wiring 21 states (20 + Final) by hand for each new user would be error-prone. The factory centralises it.
  - **What was changed:** ~50 lines of new code that loop through blocks 9 → 0, building Confirm then Conv states, wiring guards and actions.
  - **How it works now:** Call `createBiographerAgent(dto)`, get back a fully-wired Agent with `initialState = Block 1 Conv` and storage attached. Ready to call `.start()`.
  - **Why backwards:** Each `Transition` constructor requires its `subsequentState` to already exist. Starting at Final means every state has its successor at construction time. See the [Deep Dive section](#deep-dive-the-20-state-biographer-architecture) for the full explanation.

- **New helper `buildBlockPrompts()`:**
  - **Why needed:** 70 separate prompt strings (10 × 7) had to live somewhere. Inlining them in the factory would make it unreadable.
  - **What was changed:** A new ~400-line helper method that returns `String[10][7]` filled with all prompts.
  - **How it works now:** The factory calls `buildBlockPrompts(language, nickname)` once at the start, then references `prompts[i][0..6]` while building each block's states.

- **New `userId` field in `Agent`:**
  - **Why needed:** PROMISE had no concept of users. We needed to know which Supabase user owns which Biographer.
  - **What was changed:** One field + getter + setter in `Agent.java`. Hibernate auto-adds the column on first deploy.
  - **How it works now:** The factory sets `userId` from the DTO. Later, [`UserLogController`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java) filters by it (`WHERE user_id = ?`).

**Where the data goes — Supabase fields written in this step:**

| Table | Column | Source | Why |
|---|---|---|---|
| `agent` *(Hibernate, auto-created)* | `id` | Generated UUID by Hibernate | Primary key of the new agent |
| `agent` | `user_id` | DTO field, set by `AgentMetaUtility.createBiographerAgent()` | Oblivio's multi-user link |
| `agent` | `current_state_id`, `initial_state_id` | Block 1 Conv state UUID | Where the agent starts |
| `state` (×21 rows) | one row per state | Built backwards inside the factory | The 20 conversation states + 1 Final |
| `prompt` (×N rows) | one per prompt component | The 7 sections × 10 blocks | Where the actual prompt text lives |
| `transition`, `prompt_transitions` | wiring rows | Built by the factory | Links between states |
| `storage`, `storage_entry` | empty initially | Created by factory | Will hold the 10 block summaries later |
| `user_agents` *(Oblivio, frontend write)* | `user_id` | `currentUser.id` | Owner of the agent |
| `user_agents` | `agent_id` | The UUID returned by `POST /agent/biographer` | Cross-reference |
| `user_agents` | `language` | `localStorage('oblivio_language')` | One agent per (user, language) |
| `user_agents` | `nickname` | From `user_profiles.nickname` | Lets us detect "user changed their nickname → start fresh agent" |
| `user_agents` | `is_active` | `true` at creation | Soft-delete flag |

**How to add a new field that the backend should remember per agent:**
1. Add the column to the Java entity class (e.g. `Agent.java`) with a `@Column` annotation.
2. Restart the backend once with `ddl-auto=update` — Hibernate adds the column automatically.
3. Set the value in `AgentMetaUtility.createBiographerAgent()` so every new agent has it populated.

**How to add a new field that the frontend should remember per agent** (without backend changes):
1. `ALTER TABLE user_agents ADD COLUMN your_field TEXT;` in Supabase.
2. Add the key to the `user_agents.upsert(...)` call in [`biographer-promise.js:85`](Website-template/js/biographer-promise.js#L85).

---

### Step 5 — The 10 Blocks (Conversation + Confirmation)

**What happens:** For each of 10 thematic blocks, the user goes through two states: a **conversation state** (AI asks all the block's questions, e.g. "Tell me about your childhood…") and a **confirmation state** (AI summarises what it heard, user confirms or corrects).

#### 5a. Conversation Phase

**What this step needs to work:**
- Step 4 must have succeeded: there must be an `agent_id` in `user_agents` for the current user.
- The OpenAI key (`OPENAI_API_KEY` env var on Railway) must be funded — every user message triggers at least one GPT-4o call.
- Network connectivity to Railway. If Railway is cold-starting, the first message may take 5–10 seconds; the frontend has no spinner timeout intentionally, because cancelling mid-cold-start corrupts the conversation history.
- Optional: `chat_messages` and `biographer_conversations` tables in Supabase. If they don't exist, the conversation **still works** (PROMISE's own `utterance` table is the source of truth), but a refresh loses the chat history because the frontend uses `chat_messages` to repopulate the UI.

**What PROMISE does on every `/respond` call (concretely):**

This is the core PROMISE loop, identical for Biographer and Legacy Chat. One HTTP request triggers this whole chain:

1. **`AgentController.respond()`** loads the agent from Supabase via `AgentRepository.findById(agentId)` — Hibernate hydrates the agent, its `currentState`, that state's `utterances`, all transitions, decisions, actions, and the storage, into Java objects.
2. **`Agent.respond(userSays)`** delegates to `currentState.respond(userSays)`. Wrapped in a try/catch for `TransitionException` (see step 6 below).
3. **`State.respond()`** does the heavy lifting in strict order:
   a. **`acknowledge(userSays)`** — appends the user message to `utterances` AND iterates over the state's transitions. For each transition, `Transition.decide()` runs every `Decision` object on it (AND-combined). For Oblivio that's a single `StaticDecision` per transition with a prompt like "Have all 11 questions been answered yes/no?" — evaluated by `LMOpenAI.decide()` (one extra GPT-4o call).
   b. **`compactIfNeeded()`** *(Oblivio addition)* — if >20 user messages, summarises older ones and replaces them.
   c. **`composeTotalPrompt()`** — builds the final system prompt by combining the state's static prompt with any storage variables (e.g. `{{nickname}}`).
   d. **`LMOpenAI.complete()`** — sends `system prompt + utterances` to GPT-4o, gets the assistant text back.
   e. **`utterances.appendAssistantSays()`** — stores the assistant message as a new row.
4. **If a transition's `decide()` returned `true`**, `Transition.action()` runs every `Action` object on it sequentially (e.g. `TransferUtterancesAction`, `StaticExtractionAction`), then `TransitionException` is thrown carrying the target state. `Agent.respond()` catches it, sets `currentState = exception.getSubsequentState()`, and **recursively calls `respond()` again** so cascading transitions (Confirm → next Conv) happen in one HTTP call.
5. **`AgentController.save(agent)`** — Hibernate cascades the changes: new `utterance` rows are INSERTed, `agent.current_state_id` is UPDATEd, `storage_entry` rows are INSERTed if any action extracted data. One transaction; if anything fails the whole turn rolls back.
6. **Returns** the assistant text wrapped in a `ResponseView` along with `agent.isActive()` (which is `false` only when in the Final state).

The key insight: **PROMISE handles the entire state machine for you.** Oblivio only injects the prompts and reads back the storage at the end; it never has to write the "which state am I in / when do I transition" logic itself.

**Frontend files in detail:**

| File | What it specifically does | Why it's designed this way |
|---|---|---|
| [`Website-template/biographer.html`](Website-template/biographer.html) | The chat UI. Listens to the send-button click and Enter key, appends each user message to the chat bubble area, then calls `sendMessage()` from biographer-promise.js. Also renders the progress bar (1/10 to 10/10) by tracking which block the current state belongs to, and provides a voice-input button using the Web Speech API. | **Optimistic UI**: the user's message appears in the chat immediately, before the backend responds — feels faster. The progress bar uses the backend's state name (e.g. "Block 5 - Emotionen") to figure out progress; no client-side counter that could drift out of sync. Web Speech API instead of a custom audio pipeline because every modern browser supports it natively. |
| [`Website-template/js/biographer-promise.js`](Website-template/js/biographer-promise.js) | Contains `sendMessage(agentId, content)` which POSTs to `/{agentId}/respond` and returns the assistant's reply text. Also `getState(agentId)` to check which block the agent is in (used to update the progress bar). | **Stateless helper functions** (each call carries the agent ID) rather than a singleton client class, so the page can manage multiple agents in parallel if needed. Centralising HTTP calls means error handling (network failure, 5xx) is in one place. |

**Backend / Railway files in detail:**

| File | What it specifically does | Why it's designed this way |
|---|---|---|
| [`controllers/AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) | Spring `@RestController`. The `respond()` method receives the agent UUID and the user's message, loads the agent from the database via `AgentRepository.findById()`, calls `agent.respond(userSays)`, persists the modified agent, and returns a `ResponseView` with the assistant text. | **Load → modify → save** on every request because Railway containers are stateless; the agent's state lives in the database, not in memory. This means deploying a new version mid-conversation doesn't break anything — the next message simply reloads the agent on the new container. |
| [`model/Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) | Top-level entity. Its `respond()` method delegates to `currentState.respond()` and catches `TransitionException` — when caught, it updates `currentState` to the transition's target state and re-runs `respond()` recursively. This enables silent multi-step transitions (e.g. through a Confirm-state's transition into the next block's Conv-state). | **Exceptions as control flow** is unusual but elegant here: a Transition is conceptually an interruption of the normal "generate response" flow. Using an exception lets each level (Action, Transition, State, Agent) bubble the event up cleanly without polluting return types. The recursion handles cascades automatically — if a Confirm-state guard fires AND the next Conv-state is auto-entering, both transitions happen in one HTTP call. |
| [`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) | The conversation state. Its [`respond()`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L168) method does the heavy lifting: calls `acknowledge(userSays)` to append the message and check transitions, then `compactIfNeeded()` (Oblivio addition), then `composeTotalPrompt()` to build the system prompt, then `LMOpenAI.complete()` to get GPT-4o's response, then `utterances.appendAssistantSays()` to record it. | **The strict ordering matters**: `acknowledge()` must run first because transitions are evaluated against the new user message — checking transitions before adding the message would always miss the latest user input. `compactIfNeeded()` runs second, before the LLM call, so the request payload is already trimmed. Composing the prompt is last so it includes any compaction summary. |
| [`model/Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java) | The conversation history container. `appendUserSays()` and `appendAssistantSays()` add messages. [`compactIfNeeded()`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) (Oblivio's addition) summarises older messages when >20 user messages accumulate. Each `Utterance` is its own row in the database, linked back via `@ManyToOne`. | **Per-message rows** (instead of one big text blob) so a single message can be inspected, edited, or deleted without rewriting the whole conversation. Also means JPA's orphan-removal can drop individual older utterances during compaction. The `stateName` column tracks which state each message came from — useful for analytics and debugging. |
| [`spi/LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) | The OpenAI bridge. `complete()` sends conversation + system prompt to GPT-4o and returns the assistant's text. Used 1+ times per user message. Also exposes `decide()` (for Guards), `extract()` (for JSON extraction), and `summariseOffline()` (Oblivio's addition for compaction). | **One central wrapper** so when the OpenAI API changes (it has changed several times) or we want to switch to a different LLM provider (Anthropic, local model), only this file needs an edit. The whole rest of PROMISE works with Java types and is provider-agnostic. |
| [`model/Transition.java`](src/main/java/ch/zhaw/statefulconversation/model/Transition.java) | Links two states. Its `decide()` iterates through all `Decision` objects on the transition and AND-combines their boolean results. If true, `action()` runs all `Action` objects sequentially. Stock PROMISE — no Oblivio changes. | **AND-logic on decisions** means complex multi-condition transitions are possible without nested if-statements: just add more Decision objects to the list. **Sequential actions** matter because one action's effect (e.g. extracting JSON to storage) may be needed by the next action (e.g. a follow-up extraction). Parallel execution would be unsafe. |
| [`model/commons/decisions/StaticDecision.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/decisions/StaticDecision.java) | A concrete Decision implementation. Its prompt is fixed at construction time (e.g. "Have all 11 questions been asked?"). When `decide()` runs, it sends this prompt + the conversation history to `LMOpenAI.decide()` which expects a `true`/`false` response from GPT-4o. | **LLM-evaluated guards** (instead of hand-coded rules) because checking "have all 11 questions been asked" with regex or keywords is brittle — the user might paraphrase questions, change the order, or answer in unexpected ways. GPT-4o understands intent and handles all of this. The trade-off is one extra OpenAI call per guard check, but reliability beats cost here. |
| [`model/commons/actions/TransferUtterancesAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/TransferUtterancesAction.java) | Action used when transitioning Conv → Confirm. Copies all utterances from the current state into the target Confirm state, so the Confirm state has the conversation context to summarise. | **Explicit copy instead of shared reference** so the Confirm state has its own conversation history that can be independently modified or compacted. Sharing the same `Utterances` object would mean changes in one state could surprise the other. The copy is cheap (just creating new `Utterance` rows pointing to the new container). |
| [`repositories/AgentRepository.java`](src/main/java/ch/zhaw/statefulconversation/repositories/AgentRepository.java) | After `agent.respond()` returns, `repository.save(agent)` cascades through every modified entity — the updated `currentState`, the new utterance rows, any storage changes — and writes them all in one Hibernate transaction. | **Cascade-save in one transaction** for the same atomicity reason as in Step 4: a partial save (e.g. user message persisted but assistant response not) would corrupt the conversation. Either the whole turn is saved or nothing — and the next attempt restarts cleanly. |

**PROMISE adaptations — what was changed and why it works now:**

- **Context Compaction (new method):**
  - **Why needed:** In PROMISE, every new message re-sends the **complete** conversation history of the current state to GPT-4o. Cost grows linearly with conversation length; GPT-4o's 128k context window eventually overflows on very long chats.
  - **What was changed:** Added [`Utterances.compactIfNeeded()`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) (~60 lines). When the count of user messages in a single state exceeds 20, older messages are summarised to 3-5 sentences and replaced with a single system message tagged `[Zusammenfassung des bisherigen Gesprächs]`.
  - **How it works now:** First check counts user messages; if ≤20, return immediately (almost no overhead). If >20 and not yet compacted (detected via tag), build text from older messages, call `LMOpenAI.summariseOffline()`, delete the originals via JPA orphan-removal, prepend the new system message. The most recent 10 messages stay verbatim so recent context is never lost.
  - **Effect:** A 60-message state sends ~10 verbatim messages + one summary ≈ 1100 tokens — same as a 10-message state. **Cost stays flat regardless of conversation length.**
  - **When it actually fires in practice:** Compaction is a generic mechanism in `State.respond()` and is therefore active in **every** state of every agent. **But** because each Biographer block is its own state (utterances reset across blocks), a single block rarely hits the 20-message threshold — Biographer blocks are designed to finish in 10–15 turns. The mechanism is therefore mostly relevant for the **Legacy Chat** (Step 14), which is a single state where messages accumulate over the entire conversation. There compaction is critical: a 100-message persona chat would otherwise resend ~10,000 input tokens per turn.

- **Compaction trigger in `State.respond()`:**
  - **Why needed:** The method has to run **before each LLM call**, otherwise the next request still uses the un-compacted history.
  - **What was changed:** One single line inserted in [`State.respond()`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L171): `this.utterances.compactIfNeeded();`
  - **How it works now:** Every state in every agent (Biographer, Legacy, future types) automatically gets context compaction. One place, full coverage, zero per-state configuration.

- **New `summariseOffline()` method in LMOpenAI:**
  - **Why needed:** PROMISE's existing `summarise()` returns a JSON object (good for structured data). Injecting JSON into a chat as if it were dialogue confuses GPT-4o in subsequent turns ("why is there a JSON object here?").
  - **What was changed:** Added a new method that skips the JSON-format instruction and returns plain text.
  - **How it works now:** The compaction method gets back a clean German paragraph that reads naturally when placed at the start of the message list.

- **TEXT columns instead of VARCHAR(10000):**
  - **Why needed:** Persona prompts in Oblivio reach 15,000–22,000 characters (six sections of detailed personality + behaviour rules). Inserting them into VARCHAR(10000) throws `value too long for type character varying(10000)` and crashes the whole transaction.
  - **What was changed:** Replaced `@Column(length = 10000)` with `@Column(columnDefinition = "TEXT")` in three Java entity files.
  - **How it works now:** PostgreSQL's `TEXT` type has no size limit and no performance penalty. Long prompts insert and query cleanly.
  - **Stolperstein:** Hibernate's `ddl-auto=update` adds new columns automatically but does **not** change existing column types. The first deploy after the change required manual `ALTER TABLE prompt ALTER COLUMN prompt TYPE TEXT;` in Supabase.

- **PostgreSQL driver in `pom.xml`:**
  - **Why needed:** Supabase exclusively provides PostgreSQL; PROMISE shipped with MySQL Connector. Without the swap, Spring Boot crashes at startup with `Driver org.postgresql.Driver claims to not accept jdbcUrl`.
  - **What was changed:** Removed `<dependency>mysql-connector-j</dependency>`, added `<dependency>org.postgresql:postgresql</dependency>`.
  - **How it works now:** JDBC opens connections to `jdbc:postgresql://...supabase.co:5432/postgres` and Hibernate uses the PostgreSQL dialect for SQL generation.

**Where the data goes — Supabase fields written per user message:**

| Table | Column | Source | Notes |
|---|---|---|---|
| `utterance` *(Hibernate)* | `content` | `userSays` body of the POST + the GPT-4o response | One row per message. Two new rows per turn (user + assistant). |
| `utterance` | `role` | `"user"` or `"assistant"` | Set by `appendUserSays()` / `appendAssistantSays()` |
| `utterance` | `state_name` | The current state's name | Lets you query "all messages from Block 5" |
| `utterance` | `utterances_id` | FK to `utterances` row | One container per state |
| `utterances` | `id` | UUID | One per state's conversation history |
| `agent` | `current_state_id` | Updated when a transition fires | Tracks where in the 20-state machine the user is |
| `chat_messages` *(Oblivio, optional)* | `user_id`, `agent_id`, `role`, `content`, `created_at` | Written from [`biographer-promise.js`](Website-template/js/biographer-promise.js) after every successful turn | Used for fast UI reload on refresh |
| `biographer_conversations` *(Oblivio, optional)* | `user_id`, `agent_id`, `block_number`, `state_name`, `role`, `content` | Also written from `biographer-promise.js` | Richer than `chat_messages` — has block + state context for analytics |

Plus: every user message is sent to **OpenAI's `chat/completions` endpoint** along with the system prompt for the current state. The response is the assistant text shown in the chat.

**How to add a new column to track something per Biographer message** (e.g. token count, sentiment):
1. `ALTER TABLE chat_messages ADD COLUMN token_count INTEGER;` (or whichever table fits).
2. In [`biographer-promise.js`](Website-template/js/biographer-promise.js) → `saveChatMessage()`, extend the inserted object with the new key.
3. If the value comes from the backend (e.g. response.usage.total_tokens), pass it through the `sendMessage` return value first.

#### 5b. Transition: Conversation → Confirmation

When the Guard returns `true`, the transition fires:

**Backend / Railway:**
- [`model/Transition.java`](src/main/java/ch/zhaw/statefulconversation/model/Transition.java) — calls all decisions, then all actions
- [`model/commons/actions/TransferUtterancesAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/TransferUtterancesAction.java) — copies all conversation messages from the Conv state to the Confirm state so the Confirm state can summarise them

**PROMISE adaptations:** none — this transition is built with stock PROMISE classes; only the prompts are Oblivio-specific.

**What PROMISE does in this transition (concretely):**

The transition happens *inside* the same `/respond` call as the last Conv message — the user never sees a separate HTTP round-trip:

1. After `acknowledge()` in Step 5a, `Transition.decide()` runs the `StaticDecision` Guard: *"Have all 11 questions been answered? Reply 'yes' or 'no'."* GPT-4o evaluates the conversation and returns a boolean.
2. If `true`, `Transition.action()` runs each `Action` in order:
   - **`TransferUtterancesAction`** — copies every `Utterance` row from the Conv state's `utterances` into a fresh `utterances` container linked to the Confirm state. Two distinct `Utterances` rows now exist for that block (so the Conv history stays intact, and the Confirm state has its own independent copy).
3. `TransitionException(targetState)` is thrown. `Agent.respond()` catches it, sets `currentState = Confirm`, and calls `respond()` again recursively.
4. The Confirm state's `start()` is implicit: because the recursive `respond()` now runs inside the Confirm state, the next `composeTotalPrompt()` uses Confirm's prompt ("Summarise what you heard…") and produces the summary as the assistant message returned to the browser.

The user sent **one** message and got **one** response back — but internally the agent silently moved from Conv to Confirm.

**Where the data goes:** A copy of `utterances` is created and linked to the Confirm state in Supabase.

#### 5c. Confirmation Phase

**What happens:** The AI summarises the block (e.g. "From what you told me, I gathered that..."). The user confirms or asks for corrections. On confirmation, the AI extracts a structured JSON summary and stores it.

**Backend / Railway:**
- Same `State.respond()` flow as above, but now in the Confirm state with its own prompts
- [`model/commons/actions/StaticExtractionAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticExtractionAction.java) — when the user confirms, this action runs `LMOpenAI.extract()` which returns structured JSON and stores it in the agent's `Storage` under the key `block1` / `block2` / ... / `block10`

**What PROMISE does in this step (concretely):**

The Confirm state runs the same `State.respond()` cycle as Conv, but with a different prompt and a different exit Action:

1. User replies (e.g. *"yes, that's right"* or *"no, you missed the part about my brother"*).
2. `acknowledge()` evaluates Confirm's transition Guard — a `StaticDecision` with prompt *"Did the user confirm? yes/no"*. If `no`, the agent stays in Confirm and asks follow-up questions.
3. If `yes`, `Transition.action()` runs:
   - **`StaticExtractionAction`** — sends Confirm's conversation history to `LMOpenAI.extract()` with a schema like `{ key_themes: [...], emotional_tone: '...', notable_quotes: [...], … }`. The structured JSON is written to the agent's shared `Storage` as a new `StorageEntry` with key `block1` / `block2` / … / `block10`.
4. `TransitionException` is thrown carrying the next block's Conv state (or the Final state after block 10). The agent recursively enters the next state and starts its opening prompt.

After 10 successful Confirm→next transitions, `Storage` contains 10 `StorageEntry` rows — the raw material the frontend will fetch in Step 6 and persist as `user_legacies.legacy_data`.

**Where the data goes:**
- Supabase `storage_entry` table (PROMISE-managed) — the new JSON block summary

After Block 10's confirmation, the next transition leads to the Final state.

---

### Step 6 — All Blocks Complete: Final State + Access Code Generation

**What happens:** After Block 10 confirmation, the Biographer agent transitions to the Final state. The frontend now requests the 10 stored summaries, persists them in Supabase, and generates an access code for sharing.

**What this step needs to work:**
- All 10 blocks must have completed AND confirmed — `agent.currentState` must be the Final state. The frontend checks this via `getState(agentId)` and only triggers code generation when `state.isActive === false`.
- Tables `user_legacies` and `legacy_access_codes` must exist in Supabase.
- `legacy_access_codes.access_code` must be UNIQUE (it's the primary key in our schema) — the frontend retries with a fresh random code on collision (chance ≈ 1 in 36⁸).

**Frontend files:**
- [`Website-template/biographer.html`](Website-template/biographer.html) — handles completion UI (shows the access code with a "Copy" button and email-share button)
- [`Website-template/js/biographer-promise.js`](Website-template/js/biographer-promise.js) — `saveLegacyToSupabase()` writes the 10 summaries

**Backend / Railway:**
- [`model/Final.java`](src/main/java/ch/zhaw/statefulconversation/model/Final.java) — Final state, `isActive()` returns `false`
- [`controllers/AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) — `GET /{agentId}/storage` returns all 10 block summaries as JSON

**PROMISE adaptations:**
- None at this point — but the new endpoint `GET /{agentId}/storage` is in the original PROMISE controller and is used by Oblivio to retrieve block summaries.

**What PROMISE does in this step (concretely):**

PROMISE's role here is just to expose the storage it has been filling up since Step 5c:

1. After Block 10's Confirm transition fires, `currentState = Final`. `Final.isActive()` returns `false`, signalling "this agent is done — don't send any more user messages here".
2. The frontend calls `GET /{agentId}/storage`. The controller reads `agent.getStorage().getEntries()` and returns the list as JSON: `[{ key: "block1", value: {...} }, … { key: "block10", value: {...} }]`.
3. PROMISE's job is now finished. The Biographer agent stays in Supabase (`agent`, `state`, `utterances`, `storage*` rows are still there) but no further LLM calls happen on it. The frontend takes over: persists the 10 summaries into `user_legacies`, generates the access code, writes to `legacy_access_codes`.

**Where the data goes — Supabase fields written in this step:**

| Table | Column | Source | Why |
|---|---|---|---|
| `user_legacies` | `user_id` | `currentUser.id` | Owner |
| `user_legacies` | `agent_id` | The Biographer agent's UUID | Cross-reference |
| `user_legacies` | `legacy_data` | The JSON returned by `GET /{agentId}/storage` (all 10 block summaries as one JSONB) | The raw biography material |
| `user_legacies` | `created_at`, `completed_at` | Timestamps | Audit trail |
| `legacy_access_codes` | `access_code` | Frontend-generated random 8-char string (`A-Z + 2-9`, avoiding ambiguous chars) | The share key visitors will type |
| `legacy_access_codes` | `user_id` | `currentUser.id` | So the owner can find their own codes |
| `legacy_access_codes` | `nickname` | `localStorage('oblivio_nickname')` (also in `user_profiles`) | Shown to visitors as "you are chatting with ..." |
| `legacy_access_codes` | `language` | `localStorage('oblivio_language')` | Locks the persona's language |
| `legacy_access_codes` | `is_active` | `true` | Lets the owner deactivate the code later |
| `legacy_access_codes` | `legacy_data` | (left NULL initially — filled in Step 7) | Will hold the three persona prompts |

**How to add a "private notes" field that only the owner sees** (example): `ALTER TABLE legacy_access_codes ADD COLUMN owner_notes TEXT;` plus an `<textarea>` in `biographer.html` and one extra key in the `.insert({...})` call. RLS's existing `auth.uid() = user_id` policy already hides it from visitors.

---

### Step 7 — Persona Prompts Are Created (Manual Step)

**What happens:** From the 10 block summaries, three full persona prompts are crafted — one per conversation variant. This is currently a **manual admin step** done via SQL: the admin reads the block summaries, drafts the prompts, and inserts them into the `legacy_data` JSONB column.

**What this step needs to work:**
- Step 6 must have produced a row in `legacy_access_codes` with the persona's `access_code`.
- You (the admin) must have read access to `user_legacies.legacy_data` to see the 10 block summaries the person produced.
- You must be authenticated in Supabase as a role that bypasses RLS — typically the **service-role key** in the Supabase SQL Editor, since the policies on `legacy_access_codes` only allow `auth.uid() = user_id` to write.
- The frontend Legacy Chat (Step 12) will not start a conversation if `legacy_data` is empty or missing the prompt keys — it shows "diese Persona ist noch nicht bereit".

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

**Where the data goes — Supabase fields written in this step:**

The admin runs one `UPDATE` statement against `legacy_access_codes`, merging three keys into the JSONB column:

```sql
UPDATE legacy_access_codes
SET legacy_data = jsonb_build_object(
  'full_prompt_active',   'Du bist Maria, geboren 1965 in Bern… [SECTION:IDENTITY] …',
  'full_prompt_passive',  'Du bist Maria… (kein aktiver Gruss)…',
  'full_prompt_analysis', 'Du bist Maria… plus [SECTION:ANALYSIS] …'
)
WHERE access_code = 'VDSRMACZ';
```

| JSONB key on `legacy_access_codes.legacy_data` | Variant it powers | When the Legacy Chat reads it |
|---|---|---|
| `full_prompt_active` | Variant 2 (Active — persona greets first) | Step 12 sends it as the agent's system prompt |
| `full_prompt_passive` | Variant 3 (Passive — persona waits) | Step 12, ditto |
| `full_prompt_analysis` | Variant 1 (Analysis — with personality block) | Step 12, ditto |

If you want to automate Step 7 later, the natural place is a new backend endpoint that takes a `legacy_id`, reads `user_legacies.legacy_data` (the 10 summaries), calls GPT-4o to draft the three prompts, and writes them back to `legacy_access_codes.legacy_data`. Today that's done by hand because prompt quality benefits from human review.

---

### Step 8 — Optional: Voice and Avatar Assignment

**What happens:** Optionally, the admin records or selects an [ElevenLabs](https://elevenlabs.io) voice for the persona and uploads a profile photo. These enable audio playback and a face for the chat avatar.

**What this step needs to work:**
- (Voice) An [ElevenLabs](https://elevenlabs.io) account with at least one voice cloned or chosen — you need its `voice_id` (looks like `21m00Tcm4TlvDq8ikWAM`).
- (Voice) Railway env var `ELEVENLABS_API_KEY` set. The backend's [`TTSController`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) uses it as a bearer token; without it, audio playback returns 401 and the chat silently falls back to text-only.
- (Avatar) A way to upload the image. Hostpoint allows FTP/SFTP; Supabase Storage is an alternative if you don't have a separate host.
- Two `ALTER` statements if the columns aren't already there (the schema above already includes them):
  ```sql
  ALTER TABLE legacy_access_codes ADD COLUMN IF NOT EXISTS voice_id TEXT;
  ALTER TABLE legacy_access_codes ADD COLUMN IF NOT EXISTS avatar_url TEXT;
  ```

**Frontend files:** Avatar images are uploaded to Hostpoint at `/images/avatars/<name>.jpg` (referenced by `avatar_url`).

**Backend / Railway:** none for assignment; voice is used in Step 14 of Perspective 2.

**PROMISE adaptations:** none.

**Where the data goes — Supabase fields updated in this step:**

```sql
UPDATE legacy_access_codes
SET voice_id   = '21m00Tcm4TlvDq8ikWAM',
    avatar_url = '/images/avatars/maria.jpg'
WHERE access_code = 'VDSRMACZ';
```

| Column on `legacy_access_codes` | What it does at runtime |
|---|---|
| `voice_id` | Read by Legacy Chat (Step 14) → posted to backend → backend posts to ElevenLabs `/text-to-speech/{voice_id}` → audio returned and played in the browser |
| `avatar_url` | Read by Legacy Chat → set as `<img src>` in the chat header |

If a persona has no `voice_id`, the chat works fine — just no audio. If a persona has no `avatar_url`, the UI falls back to the persona's first initial in a coloured circle.

The persona is now fully online. The 8-character access code can be shared with loved ones.

---

## Perspective 2: The Visitor

### Step 9 — Open Legacy Chat with Access Code

**What happens:** A loved one goes to oblivio.ch/legacy.html, enters the 8-character access code, and the website loads the persona's data.

**What this step needs to work:**
- A row in `legacy_access_codes` with `is_active = true` and the typed `access_code`.
- That row must have `legacy_data` populated (Step 7 must have happened) — otherwise the chat shows "persona not ready".
- The RLS policy `public_read_active` on `legacy_access_codes` (see Schema section) must permit unauthenticated reads of active rows. Without it, the anon Supabase key gets `0 rows` and the visitor sees "invalid code" even when the code exists.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — code input form, then chat UI

**Backend / Railway:**
- Not involved. The frontend queries Supabase directly via the JS client.

**PROMISE adaptations:**
- None — this is purely frontend-to-Supabase.

**Where the data goes — Supabase fields READ (not written) in this step:**

```javascript
supabaseClient
  .from('legacy_access_codes')
  .select('nickname, language, is_active, legacy_data, user_id, avatar_url, voice_id')
  .eq('access_code', code)
  .single();
```

| Column read | Used for |
|---|---|
| `nickname` | Header "you are talking to {nickname}" |
| `language` | Sets the chat UI language + the persona's language |
| `is_active` | Hard gate — `false` means "this code is deactivated" |
| `legacy_data` | Source of the three persona prompts (Step 12) |
| `user_id` | Fallback path: if `legacy_data` is empty, frontend reads `user_legacies` filtered by this user_id |
| `avatar_url` | The persona's profile picture (Step 8) |
| `voice_id` | The ElevenLabs voice id (Step 14) |

If you want to **track who is opening which code**, add `ALTER TABLE legacy_access_codes ADD COLUMN open_count INTEGER DEFAULT 0;` and a `.update()` call here that increments it.

---

### Step 10 — Enter Visitor Info (Name, Relation, Gender)

**What happens:** Before chatting, the visitor enters their name (e.g. "Maria"), their relationship to the persona (child, friend, etc.), and their gender. The persona will know who's talking to it.

**What this step needs to work:**
- The visitor is NOT logged in (visitors are anonymous). All identification is by `visitor_id`, a UUID generated client-side and persisted in `localStorage`.
- No Supabase write happens yet — that comes in Step 14 when the first message is sent.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — visitor info form
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js) — handles the visitor info, builds the [Visitor Context block](Website-template/js/legacy-chat.js#L29) that will be injected into the persona prompt

**Backend / Railway:** none.

**PROMISE adaptations:** none.

**Where the data goes — nothing in Supabase yet:**

| Storage | Key | Value |
|---|---|---|
| `localStorage` | `oblivio_visitor_<accessCode>` | JSON: `{ name, relation, gender }` |
| `localStorage` | `oblivio_visitor_id` | UUID generated on first visit — will become the `visitor_id` column on `legacy_messages` in Step 14 |

To **persist visitor info to Supabase later** (e.g. for the persona owner to see who's been chatting): add a `visitor_profiles` table with `(visitor_id, access_code, name, relation, gender, created_at)`, then write a row here. Today this isn't done because guest identification is intentionally lightweight.

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

**What this step needs to work:**
- All three keys (`full_prompt_active`, `full_prompt_passive`, `full_prompt_analysis`) must exist in `legacy_access_codes.legacy_data`. If one is missing, switching to that variant loads `undefined` as the system prompt and the persona produces gibberish.
- No Supabase write yet — just `localStorage`.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — mode toggle buttons (~line 1054)
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js) — `getScopedVisitorId()` adds `__active`, `__passive`, or `__analysis` suffix so the three conversations stay isolated

**Backend / Railway:** none yet.

**PROMISE adaptations:** none — the variants are an Oblivio concept layered on top of PROMISE. The backend treats all three identically: a single PROMISE state machine with a different prompt.

**Where the data goes — Supabase is not touched in this step:**

| Storage | Key | Value |
|---|---|---|
| `localStorage` | `oblivio_mode_<accessCode>` | `'active'` / `'passive'` / `'analysis'` |

The chosen mode is later baked into the **`visitor_id`** column of `legacy_messages` as a `__mode` suffix (Step 14), so the same browser can keep three independent conversations with the same persona.

---

### Step 12 — Build the System Prompt + Create Legacy Agent

**What happens:** The frontend assembles the final system prompt by combining (a) the right variant's `full_prompt_*` and (b) the Visitor Context block. Then it asks the backend to create a Single-State Agent.

**What this step needs to work:**
- Railway backend reachable + CORS configured (same as Step 4).
- The persona's `full_prompt_*` field must not exceed PostgreSQL's text-column constraints — our `TEXT` columns have no limit, but at ~22k characters the LLM may struggle to follow all instructions reliably.
- `OPENAI_API_KEY` set on Railway (the agent's first call will use it).

**What PROMISE does in persona creation (concretely):**

This is the *Legacy* counterpart to Step 4. Where the Biographer needed a 21-state machine, a persona only needs **one** state — but it's still a real PROMISE agent with all the same persistence and message handling. Inside [`AgentMetaUtility.createSingleStateAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L20):

1. **One `State` is created** — its system prompt is the persona prompt the frontend posted (e.g. the full ~18k-character `full_prompt_active`). This is the entire personality definition: identity, chapters, style, examples, self-knowledge, rules.
2. **One `Transition` is wired** State → Final, guarded by a `StaticDecision` with the prompt *"Did the user say goodbye? yes/no"*. This is how the persona conversation ends gracefully when the visitor signals they're leaving.
3. **An empty `Utterances` container** is attached to the state — will fill as the conversation progresses.
4. **`agent.userId`** is set from the DTO (the persona owner's UUID), so `legacy_messages` rows can be joined back to the right owner for analytics.
5. **`agent.start()` is NOT called here** — the frontend explicitly calls `POST /{agentId}/start` in Step 13 so it can decide on the spot whether to display the greeting or filter `__WAIT__`.
6. **Returns** the new agent's UUID.

Crucially, **the Legacy agent reuses the exact same PROMISE machinery as the Biographer**. The same `AgentController.respond()`, the same `State.respond()` cycle, the same `Utterances.compactIfNeeded()`, the same Hibernate persistence. The only difference is the topology: one state instead of 21, one transition instead of 30+. This is what makes the three variants (Active / Passive / Analysis) cheap to implement — they're literally the same agent type with three different prompts.

**Frontend files:**
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js#L98) — [`buildLegacySystemPrompt()`](Website-template/js/legacy-chat.js#L98) composes the full prompt
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js#L29) — [`buildVisitorContext()`](Website-template/js/legacy-chat.js#L29) generates the visitor block in 7 languages
- [`Website-template/js/legacy-chat.js`](Website-template/js/legacy-chat.js#L176) — [`createLegacyAgent()`](Website-template/js/legacy-chat.js#L176) calls `POST /agent/singlestate` to Railway

**Backend / Railway:**
- [`controllers/AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) — endpoint
- [`controllers/AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L20) — [`createSingleStateAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L20) builds a one-state agent with one transition to Final
- [`config/WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) — CORS configuration so the Hostpoint frontend is allowed to call Railway

**PROMISE adaptations — what was changed and why it works now:**

- **CORS configuration (new file `WebConfig.java`):**
  - **Why needed:** PROMISE has no CORS configuration. Without it, browsers enforce the Same-Origin Policy: a JavaScript fetch from `oblivio.ch` to `promise-production.up.railway.app` is blocked outright with `Access-Control-Allow-Origin missing`. The Visitor's browser would see every chat request fail before it even reached the backend.
  - **What was changed:** Created a new Spring `@Configuration` class with a `WebMvcConfigurer` bean that registers `/**` for cross-origin requests with appropriate methods (GET, POST, PUT, DELETE, OPTIONS) and headers.
  - **How it works now:** On every request, Spring automatically adds the `Access-Control-Allow-*` headers. The browser sees the headers, accepts the cross-origin response, and the chat just works. A single bean activates this for all endpoints.
  - **Production trade-off:** Currently `allowedOriginPatterns("*")` for development simplicity. For tight security, would be restricted to `["https://oblivio.ch"]` only.

- **`createSingleStateAgent()` reuse with new `userId` field:**
  - **Why needed:** PROMISE's `createSingleStateAgent()` factory already builds the right structure for a one-state agent. We just needed to track which Supabase user the agent belongs to.
  - **What was changed:** No change to the factory's logic; it now also sets `agent.userId` from the DTO (added in Step 4).
  - **How it works now:** The Legacy agent inherits the multi-user infrastructure built for the Biographer. Same SQL queries (`WHERE user_id = ?`) work for both types of agents.

**Where the data goes — Supabase fields written in this step:**

| Table | Column | Source | Why |
|---|---|---|---|
| `agent` *(Hibernate)* | one new row | Backend factory | New single-state agent for this visitor + variant |
| `state` | one new row | Backend factory | The single state with the assembled persona prompt |
| `prompt` | one new row | Backend factory | The full system prompt text (15k–22k chars) |
| `utterances` | one empty container | Backend factory | Will fill with messages as the chat progresses |

No `user_agents` / `legacy_*` writes here — those happen earlier or per-message.

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

**PROMISE adaptations — what was changed and why it works now:**

- **The `__WAIT__` token mechanism (an Oblivio invention):**
  - **Why needed:** PROMISE's `State.start()` always generates a starter message — that's how the framework signals "the conversation has begun". For Variant 2 (Active mode), we want a real greeting. For Variants 1 and 3 (Analysis, Passive), we want the persona to **wait silently** until the visitor types first. But PROMISE doesn't have an "I want to wait" option; without a workaround, the persona would always greet first, ruining the design of Variants 1 and 3.
  - **What was changed:** No backend code was changed at all. The trick is purely in the **starter prompt** passed to the agent. In passive variants, the starter prompt reads: *"Respond with EXACTLY this text and nothing else: `__WAIT__`. No greeting, no explanation, no question, no emoji. Just the eight characters `__WAIT__` as your complete response."*
  - **How it works now:** GPT-4o obediently outputs the literal string `__WAIT__`. The backend stores this as a normal assistant message and returns it to the frontend. The frontend detects the `__WAIT__` token, **filters it out** of the chat display, and instead shows a hint like "Type to start the conversation". The agent stays in a valid PROMISE state — `currentState` is still active, `utterances` still has a (filtered) starter — ready to respond when the visitor finally writes.
  - **Why this is elegant:** No new state types, no new methods, no PROMISE modifications. Three different behaviours from one framework, all controlled by which prompt is loaded from `legacy_data` in Supabase.

**What this step needs to work:**
- The agent from Step 12 must exist (its UUID is in the URL of the POST).
- The variant prompt must contain explicit `__WAIT__` instructions for Variants 1/3 — otherwise GPT-4o invents a normal greeting and visitors in Passive mode see the persona "barging in".

**What PROMISE does in this step (concretely):**

1. `AgentController` receives `POST /{agentId}/start`, loads the agent from Supabase.
2. `Agent.start()` delegates to `currentState.start()`. There's only one state, so this is the persona state created in Step 12.
3. **`State.start()`** sends the state's *starter prompt* (different from the system prompt) plus the empty `utterances` to `LMOpenAI.complete()`. The starter prompt is what differentiates the variants:
   - **Active variant** — *"Greet the visitor in your own voice."* → GPT-4o produces a real greeting like *"Hey Maria, schön dich zu sehen!"*
   - **Passive / Analysis variants** — *"Respond with EXACTLY the eight characters `__WAIT__` and nothing else."* → GPT-4o returns the literal string `__WAIT__`.
4. Either response is stored as the first `utterance` row (role = assistant) and returned to the browser.
5. `repository.save(agent)` persists the new utterance.

PROMISE doesn't know anything about variants — it just sends whatever starter prompt was configured. The `__WAIT__` trick lives entirely in the Oblivio prompts plus the frontend filter; **zero backend code** was needed to implement "the persona waits silently."

**Where the data goes — Supabase fields written in this step:**

| Table | Column | Source | Notes |
|---|---|---|---|
| `utterance` *(Hibernate)* | `content` | GPT-4o's response (real greeting or literal `__WAIT__`) | Always stored, even for `__WAIT__` — frontend hides it from the UI |
| `utterance` | `role` | `'assistant'` | This is the persona's first utterance |
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

**PROMISE adaptations — what was changed and why it works now:**

- **Context Compaction is essential here** (introduced in Step 5a):
  - Unlike the Biographer (where each block is a separate state and rarely reaches 20 messages), the Legacy Chat is a **single state** where every visitor message accumulates. Without compaction, a 100-message persona chat would re-send all 100 messages to GPT-4o on every new turn — tokens (and cost) growing linearly with conversation length.
  - With compaction, after message 21 the older history is summarised once and replaced. From that point on, only ~10 recent messages plus a short summary are sent — token usage stays roughly constant regardless of how long the chat runs.
  - **This is the place where compaction actually pays off in production**, far more than in the Biographer.

- **TTS Controller (new file):** [`TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) — PROMISE has no audio output. Oblivio added this as a server-side bridge so the ElevenLabs API key stays on Railway (never exposed to the browser). Plus the new DTO [`TTSRequest.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/TTSRequest.java).

**What this step needs to work:**
- Agent exists (Step 12) + starter resolved (Step 13).
- Table `legacy_messages` must exist with the columns shown below — without it, the chat works once but cannot rehydrate on refresh (history is lost).
- `OPENAI_API_KEY` (text turn) and optionally `ELEVENLABS_API_KEY` (voice turn) on Railway.
- The RLS policy on `legacy_messages` must allow anonymous inserts when the parent `access_code` is `is_active = true` — see the Schema section's `active_code_messages` policy. Without it, every `saveMessage()` call fails silently and the chat history is lost.

**What PROMISE does in the persona conversation (concretely):**

This is the exact same `State.respond()` cycle as Step 5a — but with two important differences because the agent only has one state:

1. **No state transitions during the chat** (unless the visitor says goodbye). The agent stays in its single persona state, so `currentState` never changes between turns. Hibernate doesn't have to UPDATE `agent.current_state_id` on every save.
2. **Utterances accumulate without limit** in that one state. This is exactly where **`compactIfNeeded()` actually pays off** — unlike the Biographer where each block reset the history, here a 100-message chat would otherwise resend the entire 100-message history on every turn. With compaction, anything older than 10 messages gets summarised after message 21 → token usage stays roughly flat.
3. **The Goodbye guard** is checked every turn via `Transition.decide()` with the `StaticDecision` *"Did the user say goodbye? yes/no"*. If `true`, `TransitionException` fires and the agent moves to Final → `isActive()` becomes `false` → the frontend shows "this conversation has ended".
4. **All other PROMISE plumbing is identical** to Step 5a: load agent → `State.respond()` → append user → compact → compose prompt → `LMOpenAI.complete()` → append assistant → save → return.

What Oblivio adds on top of PROMISE here:
- The parallel write to `legacy_messages` (a denormalised, frontend-friendly history — PROMISE only writes to `utterance`).
- The optional `POST /{agentId}/tts` call to [`TTSController`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java), which is **not** a PROMISE concept at all — it's a thin proxy to ElevenLabs that keeps the API key on the server side.

**Where the data goes — Supabase fields written per visitor message:**

| Table | Column | Source on the website | Why this column |
|---|---|---|---|
| `utterance` *(Hibernate)* | `content`, `role`, `state_name`, `utterances_id` | Same as in Step 5a, but for the Legacy single state | Canonical state-machine record — used to reconstruct the agent on the next request |
| `legacy_messages` | `access_code` | URL parameter `?code=VDSRMACZ` | Foreign key to the persona |
| `legacy_messages` | `visitor_id` | `getScopedVisitorId(mode)` → e.g. `'7a2c...__active'` | Mode-scoped so the same visitor can have 3 independent conversations with the same persona |
| `legacy_messages` | `visitor_name` | `localStorage('oblivio_visitor_<accessCode>').name` (from Step 10) | Lets the persona owner see who chatted |
| `legacy_messages` | `user_id` | The persona-owner's UUID (the one stored on `legacy_access_codes.user_id`) | Lets the owner query their personas' chat history |
| `legacy_messages` | `role` | `'user'` (visitor) or `'legacy'` (persona) | Distinguishes the two parties; note: NOT `'assistant'` because this isn't the canonical PROMISE record |
| `legacy_messages` | `content` | The visitor's typed text OR the persona's GPT-4o response | The actual message |
| `legacy_messages` | `created_at` | `new Date().toISOString()` | Chronological ordering on rehydrate |

Plus: every message triggers **one GPT-4o call** (text response), optionally **one ElevenLabs call** (MP3 playback).

**To add a new per-message column** (e.g. "user gave a thumbs up"):
1. `ALTER TABLE legacy_messages ADD COLUMN feedback TEXT;` in Supabase.
2. Add a thumbs-up button to the chat UI in `legacy.html`.
3. On click, call `supabaseClient.from('legacy_messages').update({ feedback: 'positive' }).eq('id', messageId)` — the `id` must be returned from the original insert (use `.insert(row).select().single()`).

The two parallel writes (PROMISE tables + `legacy_messages`) are intentional: PROMISE tables are the canonical state-machine record (used to reconstruct the agent on the next request), while `legacy_messages` is a denormalised read-friendly history for the frontend and analytics.

---

### Step 15 — Variant Switching (Optional, Anytime)

**What happens:** Mid-conversation, the visitor clicks a different variant button (e.g. switches from Variant 2 to Variant 1). The frontend creates a **new** PROMISE agent with the new variant's prompt, includes the old conversation as context, and loads the previous history for the new mode if any exists.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — `switchMode()` function: changes `currentMode`, updates `getScopedVisitorId(newMode)`, calls `createLegacyAgent()` again with the new prompt

**Backend / Railway:**
- Same flow as Step 12: a new `POST /agent/singlestate` request creates another agent on Railway.

**PROMISE adaptations — what was changed and why it works now:**

- **Mode-scoped `visitor_id` (an Oblivio convention, no backend code change):**
  - **Why needed:** If the visitor switches from Variant 2 (where they've had a 10-message conversation) to Variant 1 (different persona behavior), should those 10 messages reappear, or should Variant 1 start fresh? Both are valid UX choices. Mixing them in one history makes the persona schizophrenic. Hard-separating them lets the visitor explore each variant cleanly.
  - **What was changed:** No backend code. The frontend's [`getScopedVisitorId(mode)`](Website-template/js/legacy-chat.js) appends `__active`, `__passive`, or `__analysis` to the base visitor UUID. Every read and write to `legacy_messages` uses the scoped ID.
  - **How it works now:** Three independent conversation histories per visitor + persona combination. The visitor can freely jump between them; messages don't bleed across modes. Variant switch creates a new PROMISE agent on the backend with the new prompt, but only the conversation history matching the new scope is loaded back.

**What this step needs to work:**
- The persona's `legacy_data` must contain the prompt for the target variant (Step 7).
- No new Supabase schema needed — the mode suffix on `visitor_id` is purely a naming convention.

**What PROMISE does in a variant switch (concretely):**

A variant switch is just **a fresh `POST /agent/singlestate` call** with a different prompt. From PROMISE's point of view there's no concept of "switching variants":

1. The old agent stays in Supabase exactly as it was — its state, utterances, and storage are untouched. The visitor could in principle return to it, but Oblivio's frontend doesn't expose that.
2. PROMISE creates a brand new `Agent` (`createSingleStateAgent()` again, same as Step 12) with the new variant's persona prompt as the system prompt.
3. The new agent's `utterances` start empty. Whatever conversation history the visitor had in the previous variant is **not** carried over inside PROMISE; instead, the frontend rehydrates the new agent visually by reading `legacy_messages` rows with the new mode-scoped `visitor_id`.

The variant switch is therefore a pure Oblivio concept layered on top of PROMISE: two unrelated PROMISE agents with two different prompts, and a frontend convention (`__active` / `__passive` / `__analysis` suffix) that keeps their histories separate in the denormalised table.

**Where the data goes — Supabase reads and writes change scope:**

| Operation | Effect |
|---|---|
| Read on `legacy_messages` | Filter becomes `WHERE access_code = ? AND visitor_id = '<uuid>__<newmode>'` — only that variant's history is shown |
| Write on `agent`, `state`, `prompt`, `utterances` (Hibernate) | New rows for the new agent (the old agent stays — visitor can switch back) |
| Write on `legacy_messages` | New rows from now on carry the new `visitor_id` suffix |

Old agent stays untouched (the visitor can return at any time). If you want to **clean up old agents**, run `DELETE FROM agent WHERE user_id IS NULL AND created_at < NOW() - INTERVAL '30 days'` — but only after backing up `legacy_messages`, which holds the human-readable record.

---

### Step 16 — Session Ends

**What happens:** The visitor closes the browser, or the persona's "goodbye" guard fires and transitions the agent to Final. Either way, the conversation history is persisted.

**Frontend files:**
- [`Website-template/legacy.html`](Website-template/legacy.html) — `loadConversationHistory()` rehydrates the chat on the next visit

**Backend / Railway:**
- [`model/Final.java`](src/main/java/ch/zhaw/statefulconversation/model/Final.java) — Final state if goodbye triggered
- [`model/commons/actions/StaticExtractionAction.java`](src/main/java/ch/zhaw/statefulconversation/model/commons/actions/StaticExtractionAction.java) — extracts a summary of the whole legacy chat to storage (optional analytics)

**PROMISE adaptations:** none — final-state handling is stock PROMISE.

**What this step needs to work:**
- Nothing extra — closing the tab requires no explicit "save" because every message was written immediately in Step 14.
- On return, the rehydrate query needs the SAME `visitor_id` (from `localStorage`) to find the previous history. Clearing browser storage = starting a fresh conversation.

**What PROMISE does when the session ends (concretely):**

Almost nothing — and that's the point. PROMISE was designed to be stateless on the request level:

1. **If the visitor just closes the tab**, no signal reaches PROMISE at all. The agent stays in Supabase exactly as it was after the last `repository.save(agent)`. When the visitor returns days later, `AgentController.respond()` loads the same agent on the next request and the conversation continues seamlessly.
2. **If the Goodbye Guard fired** (the visitor said "tschüss"), `TransitionException` moved the agent to `Final`. The `StaticExtractionAction` on that transition can optionally extract a chat summary to the agent's `Storage`. From then on `agent.isActive()` returns `false` and the frontend shows the chat as ended.
3. **On the next page load**, the frontend doesn't ask PROMISE for history — it reads `legacy_messages` from Supabase directly (cheaper and faster than calling `GET /{agentId}/conversation`). PROMISE only re-enters the picture when the visitor sends a new message.

This is why Railway containers can be cold-started or replaced mid-conversation without breaking anything: **the agent's entire state lives in Supabase, not in JVM memory.** Every request is a fresh load → modify → save cycle.

**Where the data goes — nothing new is written when the visitor leaves:**

| Source | Persists where |
|---|---|
| Every message that was sent | Already in `utterance` (PROMISE) + `legacy_messages` (Oblivio) |
| The current `currentState` of the agent | `agent.current_state_id` |
| Visitor identity (name, relation, gender) | `localStorage` on this device only |

On rehydrate, the frontend reads:

```javascript
supabaseClient.from('legacy_messages')
  .select('role, content, created_at')
  .eq('access_code', accessCode)
  .eq('visitor_id', getScopedVisitorId(mode))
  .order('created_at', { ascending: true });
```

Different devices show **different histories** because `visitor_id` is per-browser via `localStorage`. If you want cross-device history, you would have to introduce visitor login (Supabase Auth for visitors too) and key history off the user UUID instead of the localStorage UUID.

---

## PROMISE Adaptations: What Was Changed and Why

This section explains **every modification made to PROMISE** in detail — what was changed, why it was necessary, and what would break without it.

### Adaptation 1: Database driver swap (MySQL → PostgreSQL)

**File:** [`pom.xml`](pom.xml)

**Before:** PROMISE shipped with the MySQL Connector dependency.

**After:** Replaced with `org.postgresql:postgresql`.

**Why:** Supabase only offers PostgreSQL. Without this swap, Spring Boot would crash on startup with `Driver org.postgresql.Driver claims to not accept jdbcUrl`. PostgreSQL is also a stronger fit for our use case because of native JSONB support (used heavily for `legacy_data`, `legacy_messages`, etc.).

---

### Adaptation 2: Database column types — VARCHAR(10000) → TEXT

**Files:**
- [`model/Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java) — `prompt` column
- [`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java) — `starterPrompt`, `summarisePrompt`
- [`model/Utterance.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterance.java) — `content`

**Before:** `@Column(length = 10000)` (VARCHAR with hard cap at 10000 characters)

**After:** `@Column(columnDefinition = "TEXT")` (PostgreSQL TEXT, no size limit)

**Why:** Persona prompts (full IDENTITY + CHAPTERS + ANALYSIS + STYLE + EXAMPLES + SELF_KNOWLEDGE + RULES) routinely exceed 15,000 characters — the longest seen is around 22,000. Inserting these would crash with `value too long for type character varying(10000)`. PostgreSQL TEXT has no length limit and no performance penalty.

**Stolperstein:** Hibernate's `ddl-auto=update` mode **does not change existing column types** even if the Java annotation changes. If a database was previously running with VARCHAR(10000), you must manually run `ALTER TABLE prompt ALTER COLUMN prompt TYPE TEXT` etc. in Supabase.

---

### Adaptation 3: Multi-user support — `userId` on Agent

**File:** [`model/Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java)

**Added:** A `private String userId` field with getter and setter.

**Why:** PROMISE was designed for single-user scenarios. All agents lived in one shared table with no user attribution. Oblivio is multi-user — different people simultaneously running their own Biographer sessions. Without `userId`, there is no way to filter "show me only my agents" via SQL, and the journey-dashboard page can't render a per-user history. This field is populated automatically in `AgentMetaUtility` when an agent is created with a `userId` in the DTO.

**Consequence:** A new `UserLogController` was created to expose `/user/{userId}/agents`, `/user/{userId}/conversations`, and `/user/{userId}/stats`.

---

### Adaptation 4: Context Compaction — `Utterances.compactIfNeeded()`

**File:** [`model/Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) (~60 new lines)

**What it does:** Counts user messages in the conversation. If more than 20, it takes everything except the last 10 messages, sends them to GPT-4o with a prompt like *"Summarise this conversation in 3-5 sentences"*, and replaces the old messages with one single system message containing the summary (prefixed with `[Zusammenfassung des bisherigen Gesprächs]`). On subsequent compaction checks, the prefix tells the function to skip — no double-compaction.

**Why:** Without compaction, every new message resends the **entire** conversation to GPT-4o. A 50-message conversation sends ~5000 input tokens *on every turn*. At GPT-4o pricing, a single long legacy chat could cost several dollars. Beyond cost, GPT-4o's 128k context window eventually overflows on very long chats. Compaction keeps cost roughly **constant** regardless of conversation length, while preserving the gist of older messages.

**Threshold values:**
- `USER_MESSAGE_COMPACT_THRESHOLD = 20` — empirically chosen: early enough to control cost, late enough to preserve natural context
- `MESSAGES_TO_KEEP = 10` — the most recent N messages stay verbatim

---

### Adaptation 5: One-line trigger in `State.respond()`

**File:** [`model/State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L171)

**Added:** A single call `this.utterances.compactIfNeeded();` right after `acknowledge()`.

**Why:** Compaction must run **before the next LLM call**, so the compacted utterances are sent to GPT-4o on the next turn instead of the full history. Placing it in `State.respond()` means every conversation in every state automatically benefits — Biographer, Legacy chat, even future agent types. **One line, one place, full coverage.**

---

### Adaptation 6: Plain-text summary method — `LMOpenAI.summariseOffline()`

**File:** [`spi/LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java)

**Added:** A new method `summariseOffline()` (~10 lines).

**Why:** PROMISE's existing `summarise()` method returns a JSON object — useful for structured data extraction, but useless for compaction because injecting JSON into the conversation history would confuse the LLM in subsequent turns. The new method skips the "return JSON" instruction and returns a plain-text string, ready to be wrapped in `[Zusammenfassung des bisherigen Gesprächs]` and prepended to the message list.

---

### Adaptation 7: Biographer factory — `createBiographerAgent()`

**File:** [`controllers/AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64) (~50 lines for the factory + ~400 lines of block prompts)

**Added:** Two new public methods, three helpers, and 70 prompt strings (10 blocks × 7 components).

**Why:** PROMISE provides only **bricks** — State, Transition, Decision, Action. It does not provide the **wiring**. To build the Biographer, every block needs:
- A Conv state with the right system prompt and starter prompt
- A Confirm state with the right system prompt and starter prompt
- A Guard prompt for Conv→Confirm transition
- A Guard prompt for Confirm→next-block transition
- An Extract prompt for the JSON summary
- An action that runs `TransferUtterancesAction` between Conv and Confirm
- An action that runs `StaticExtractionAction` between Confirm and the next block

Doing this 10 times by hand would be error-prone. The factory abstracts it: pass in language + nickname, get back a fully-wired 20-state agent (plus the Final end state).

**Plus the backwards-build technique** explained in detail in the [Deep Dive section](#deep-dive-the-20-state-biographer-architecture) above.

---

### Adaptation 8: New REST endpoint — `POST /agent/biographer`

**File:** [`controllers/AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) (~25 new lines)

**Added:** A new `@PostMapping("agent/biographer")` handler.

**Why:** PROMISE exposes only `POST /agent/singlestate`, which builds a single-state agent. The Biographer needs its own endpoint because:
- It accepts different DTO fields (`language`, `nickname`)
- It must validate the type as `AgentMetaType.biographer = 1` (a new enum value)
- It delegates to a different factory method

Sharing `/agent/singlestate` would require overloading it with optional fields and runtime type-checks, which is messy.

**Plus:**
- New enum value `biographer = 1` in [`AgentMetaType.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaType.java)
- New DTO [`BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) with `language` and `nickname` fields

---

### Adaptation 9: CORS configuration

**File:** [`config/WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) (new file, ~30 lines)

**Added:** A `@Configuration` class that defines CORS mappings on `/**`.

**Why:** The Oblivio frontend lives on `oblivio.ch` (Hostpoint). The backend lives on `promise-production.up.railway.app` (Railway). Browsers enforce the **Same-Origin Policy**: by default, JavaScript on one origin cannot fetch from another origin. Without CORS headers from the backend, every API call from the frontend fails with `Access-Control-Allow-Origin missing`.

The configuration tells the browser: "Yes, this Railway server accepts cross-origin requests from any origin." A single Spring Boot bean activates this for all endpoints.

---

### Adaptation 10: TTS bridge — `TTSController.java`

**File:** [`controllers/TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) (new file, ~100 lines)

**Added:** A new controller exposing `POST /{agentID}/tts?voice_id=...` that proxies requests to ElevenLabs.

**Why:** PROMISE is text-only — there is no audio. Oblivio needed personas to speak with real voices (especially important for legacy chats — a grandmother's voice carries memory the way text cannot). ElevenLabs provides voice synthesis.

**Why through the backend (instead of directly from frontend)?** Because direct frontend calls would expose the ElevenLabs API key in the browser code, where anyone could copy it and run up our bill. The backend-as-bridge pattern keeps the key safe on Railway: frontend sends plain text, backend signs the request with the secret key, frontend receives MP3 bytes.

**Plus:** A new DTO [`TTSRequest.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/TTSRequest.java).

---

### Adaptation 11: Multi-user endpoints — `UserLogController.java`

**File:** [`controllers/UserLogController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java) (new file)

**Added:** Three new endpoints:
- `GET /user/{userId}/agents` — list of all agents for a user
- `GET /user/{userId}/conversations` — all conversation messages
- `GET /user/{userId}/stats` — usage statistics

**Why:** PROMISE's `AgentController` is per-agent (`/{agentId}/respond` etc.) but has no per-user endpoints. Once we added `userId` (Adaptation 3), we need ways to query "show me everything for user X". The frontend journey-dashboard page calls these endpoints to render the user's biographer history and active personas.

**Plus:** New DTOs [`UserAgentView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserAgentView.java) and [`UserConversationView.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/UserConversationView.java).

---

### Adaptation 12: Live log streaming — `logging/` package

**Files (4 new classes in a new package):**
- [`logging/LogEvent.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogEvent.java) — DTO
- [`logging/SseLogAppender.java`](src/main/java/ch/zhaw/statefulconversation/logging/SseLogAppender.java) — custom Logback appender
- [`logging/LogStreamBroadcaster.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamBroadcaster.java) — broadcaster to SSE subscribers
- [`logging/LogStreamController.java`](src/main/java/ch/zhaw/statefulconversation/logging/LogStreamController.java) — `GET /logs/stream` endpoint

**Why:** When something goes wrong in production on Railway, traditional ways of getting logs require either SSH access or the Railway CLI — neither is convenient during a live debugging session. With the live log stream, opening `/logs/stream` in any browser immediately shows in real-time what the state machine is doing. This was especially helpful while debugging the cascading-transition bug: watching guards fire in sequence revealed exactly which condition was misfiring.

**Why SSE (Server-Sent Events) and not WebSockets?** SSE is unidirectional (server → browser only), requires no special client libraries, and survives automatic reconnects. For log streaming we never need to push *from* the browser, so SSE is the simpler choice.

---

### Adaptation 13: Production properties

**Files:**
- [`src/main/resources/application-prod.properties`](src/main/resources/application-prod.properties) (new)
- [`src/main/resources/openai-prod.properties`](src/main/resources/openai-prod.properties) (new)

**Why:** PROMISE ships with only local-development properties — database URL, password, API key are hard-coded. Pushing those to GitHub would leak credentials. Spring Boot's "profiles" mechanism allows different property files per environment: `application-prod.properties` is loaded only when `SPRING_PROFILES_ACTIVE=prod` is set. By using `${ENV_VAR}` placeholders, actual values come from Railway's environment variables at runtime — keeping passwords out of the repository entirely.

**Special setting `prepareThreshold=0`:** Essential because Supabase uses PgBouncer in transaction-pooling mode, which is incompatible with PostgreSQL's prepared statements. Without this setting, queries would intermittently fail with `prepared statement does not exist`.

---

### Adaptation 14: Containerization for Railway

**Files (all new):**
- [`Dockerfile`](Dockerfile) — multi-stage build
- [`railway.json`](railway.json) — Railway deployment config
- [`.railwayignore`](.railwayignore) — files excluded from build

**Why:** PROMISE has no Dockerfile — it's meant for local `mvn spring-boot:run`. Railway needs an image to deploy. The **multi-stage build** is deliberate:

- **Stage 1:** Includes the full JDK and Maven Wrapper (~700 MB) — needed only to compile the JAR
- **Stage 2:** Includes just the JRE and the compiled JAR (~200 MB) — what actually runs in production

The smaller final image starts faster, uses less memory, and has fewer attack vectors than a JDK. The `USER nobody` line follows the principle of least privilege — if a vulnerability is exploited, the attacker has no shell privileges. The healthcheck lets Railway automatically restart the container if it stops responding.

---

### Adaptation 15: Logback configuration update

**File:** [`src/main/resources/logback-spring.xml`](src/main/resources/logback-spring.xml)

**Added:** Registration of the new `SseLogAppender` from Adaptation 12.

**Why:** The custom appender (`logging/SseLogAppender.java`) must be told to Logback or it won't actually receive log events. This is the connection point between PROMISE's logging system (Logback) and the new SSE infrastructure.

---

### Summary Table: All PROMISE Adaptations

| # | What | File | Why |
|:--:|---|---|---|
| 1 | MySQL → PostgreSQL | [`pom.xml`](pom.xml) | Supabase requires PostgreSQL |
| 2 | VARCHAR → TEXT | [`Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java), [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java), [`Utterance.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterance.java) | Persona prompts >10k chars |
| 3 | `userId` field | [`Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) | Multi-user support |
| 4 | Context Compaction | [`Utterances.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) | Linear token cost growth |
| 5 | Compaction trigger | [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L171) | Call before each LLM call |
| 6 | `summariseOffline()` | [`LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) | Plain text for compaction |
| 7 | Biographer factory | [`AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64) | Build 20-state agent + Final + 70 prompts |
| 8 | `/agent/biographer` endpoint | [`AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) | New agent type API |
| 9 | CORS | [`WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) | Frontend-backend separation |
| 10 | TTS bridge | [`TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) | Hide ElevenLabs API key |
| 11 | Multi-user endpoints | [`UserLogController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java) | Per-user data queries |
| 12 | Live log streaming | [`logging/`](src/main/java/ch/zhaw/statefulconversation/logging/) (4 files) | Production debugging |
| 13 | Production properties | [`application-prod.properties`](src/main/resources/application-prod.properties), [`openai-prod.properties`](src/main/resources/openai-prod.properties) | Credentials via env vars |
| 14 | Containerization | [`Dockerfile`](Dockerfile), [`railway.json`](railway.json), [`.railwayignore`](.railwayignore) | Railway deployment |
| 15 | Logback config | [`logback-spring.xml`](src/main/resources/logback-spring.xml) | Register SSE appender |

**Lines of code added:** ~1,000 (mostly in `AgentMetaUtility.java` for the 70 block prompts)
**Lines of code modified:** ~10 (one-line additions in `State.respond()`, `Agent.java`, `AgentMetaController.java`)
**New Java files:** 11
**Configuration / infrastructure files added:** 7

---

## Appendix: PROMISE Adaptations at a Glance

A compact reference grouped by concern. Each row links to the file that was added or modified. The 15 adaptations from the previous section are the same items — this view organises them by *what they enable* rather than by chronological order.

### Database layer (Adaptations 1–2)

| # | What | File | Why |
|---|---|---|---|
| 1 | MySQL driver → PostgreSQL | [`pom.xml`](pom.xml) | Supabase only offers PostgreSQL |
| 2 | `VARCHAR(10000)` → `TEXT` | [`Prompt.java`](src/main/java/ch/zhaw/statefulconversation/model/Prompt.java), [`State.java`](src/main/java/ch/zhaw/statefulconversation/model/State.java), [`Utterance.java`](src/main/java/ch/zhaw/statefulconversation/model/Utterance.java) | Persona prompts reach 15k–22k characters |

### Multi-user support (Adaptation 3)

| # | What | File | Why |
|---|---|---|---|
| 3 | `userId` field on Agent | [`Agent.java`](src/main/java/ch/zhaw/statefulconversation/model/Agent.java) | PROMISE knew no users; Oblivio needs "show me only my agents" |

### Context Compaction (Adaptations 4–6)

| # | What | File | Why |
|---|---|---|---|
| 4 | New `compactIfNeeded()` method (~60 lines) | [`Utterances.java:118`](src/main/java/ch/zhaw/statefulconversation/model/Utterances.java#L118) | Tokens stay flat even on 100-message chats |
| 5 | Single-line trigger in `respond()` | [`State.java:171`](src/main/java/ch/zhaw/statefulconversation/model/State.java#L171) | Activates compaction for every agent type |
| 6 | `summariseOffline()` (plain text, not JSON) | [`LMOpenAI.java`](src/main/java/ch/zhaw/statefulconversation/spi/LMOpenAI.java) | JSON injected into history would confuse GPT-4o |

### Biographer factory (Adaptations 7–8)

| # | What | File | Why |
|---|---|---|---|
| 7 | `createBiographerAgent()` + `buildBlockPrompts()` (~450 LOC) | [`AgentMetaUtility.java:64`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64) | Builds the 21-state machine + 70 prompts on the fly |
| 8 | `POST /agent/biographer` + enum + DTO | [`AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java), [`AgentMetaType.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaType.java), [`BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) | Dedicated endpoint for Biographer agent creation |

### Cross-origin + new controllers (Adaptations 9–12)

| # | What | File | Why |
|---|---|---|---|
| 9 | CORS configuration | [`WebConfig.java`](src/main/java/ch/zhaw/statefulconversation/config/WebConfig.java) | Frontend on Hostpoint must reach backend on Railway |
| 10 | `TTSController` (ElevenLabs bridge) | [`TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) | Keeps the API key server-side, out of the browser |
| 11 | Multi-user endpoints (`/user/{id}/agents` etc.) | [`UserLogController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/UserLogController.java) | Lets the journey dashboard list a user's own agents |
| 12 | Live log streaming via SSE | [`logging/`](src/main/java/ch/zhaw/statefulconversation/logging/) (4 new classes) | In-browser debugging without the Railway CLI |

### Deployment (Adaptations 13–15)

| # | What | File | Why |
|---|---|---|---|
| 13 | `application-prod.properties`, `openai-prod.properties` | [`src/main/resources/`](src/main/resources/) | Credentials via env vars instead of hardcoded |
| 14 | `Dockerfile` + `railway.json` | [`Dockerfile`](Dockerfile), [`railway.json`](railway.json) | Auto-deploy to Railway, health-check, multi-stage build |
| 15 | SSE appender in Logback | [`logback-spring.xml`](src/main/resources/logback-spring.xml) | Hooks logs into the `/logs/stream` endpoint |

### What was NOT changed

Equally important: **PROMISE's state-machine engine itself is unchanged.** Multi-state support, Transitions, Decisions, Actions, Storage, Hibernate mapping — all stock PROMISE. Oblivio only builds *one concrete topology* (the 20-state Biographer) and *one Single-State agent for personas* on top of it, plus the cross-cutting concerns above.

Concretely unchanged files include:

- [`AgentController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentController.java) (all `/respond`, `/start`, `/state` endpoints)
- [`Transition.java`](src/main/java/ch/zhaw/statefulconversation/model/Transition.java), [`Decision.java`](src/main/java/ch/zhaw/statefulconversation/model/Decision.java), [`Action.java`](src/main/java/ch/zhaw/statefulconversation/model/Action.java), [`Storage.java`](src/main/java/ch/zhaw/statefulconversation/model/Storage.java), [`Final.java`](src/main/java/ch/zhaw/statefulconversation/model/Final.java)
- All [`model/commons/`](src/main/java/ch/zhaw/statefulconversation/model/commons/) actions and decisions
- All repositories ([`AgentRepository`](src/main/java/ch/zhaw/statefulconversation/repositories/AgentRepository.java), [`StateRepository`](src/main/java/ch/zhaw/statefulconversation/repositories/StateRepository.java), …)
- [`StatefulconversationApplication.java`](src/main/java/ch/zhaw/statefulconversation/StatefulconversationApplication.java) (Spring Boot entry point)

**Summary in one line:** 15 focused adaptations — 3 in the database layer, 1 for multi-user, 3 for context compaction, 2 for the Biographer factory, 4 for deployment / cross-origin / debugging, 2 for production properties — the state-machine core stays untouched.

---

## Recap: What PROMISE Provided vs What Oblivio Added

PROMISE provided the **multi-state framework** — the abstract concept of states, transitions, decisions, actions, plus the LLM glue and persistence via JPA/Hibernate. **Arbitrary multi-state agents are a built-in PROMISE feature**, not something Oblivio had to add. What Oblivio did was use these existing building blocks to construct specific applications:

- **The Biographer** (20 states + 1 end state, 70 prompts in 8 languages) — [`AgentMetaUtility.createBiographerAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64)
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
