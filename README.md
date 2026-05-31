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

**PROMISE adaptations — what was changed and why it works now:**
- **Why it was needed:** PROMISE was English-only. It has no concept of "select a target language for this agent". Without changes, every Biographer interview would default to whatever language is hard-coded in the prompts.
- **What was changed:** A new method [`getLanguageInstruction()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L480) was added in `AgentMetaUtility.java`. The factory accepts a `language` parameter from the DTO. For each of the 70 block prompts, the language instruction is prepended at build time.
- **How it works now:** The prompts themselves stay written in **German** (one set, 70 strings). At runtime, the language prefix tells GPT-4o: *"The following instructions are in German, but you MUST communicate exclusively in Korean. Translate all questions and answers to Korean."* GPT-4o handles the translation on the fly. This saves us from maintaining 8 × 70 = 560 strings; we only need 70 + 8 (one prefix per language).
- **Trade-off:** Slightly higher tokens per response (the prefix is ~80 tokens) in exchange for one canonical source of truth. Translation quality is excellent because GPT-4o is multilingual-native.

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

**What happens:** Frontend sends a POST request to the backend asking for a new Biographer. The backend builds a 20-state agent + 1 end state (10 blocks × 2 + Final) on the fly and returns its ID.

**Frontend files:**
- [`Website-template/biographer.html`](Website-template/biographer.html) — UI flow
- [`Website-template/js/biographer-promise.js`](Website-template/js/biographer-promise.js) — API client that calls `POST /agent/biographer`

**Backend / Railway:**
- [`controllers/AgentMetaController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaController.java) — the endpoint
- [`controllers/AgentMetaUtility.java`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java) — factory method [`createBiographerAgent()`](src/main/java/ch/zhaw/statefulconversation/controllers/AgentMetaUtility.java#L64)
- [`controllers/dto/BiographerAgentCreateDTO.java`](src/main/java/ch/zhaw/statefulconversation/controllers/dto/BiographerAgentCreateDTO.java) — request body

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

**PROMISE adaptations — what was changed and why it works now:**

- **The `__WAIT__` token mechanism (an Oblivio invention):**
  - **Why needed:** PROMISE's `State.start()` always generates a starter message — that's how the framework signals "the conversation has begun". For Variant 2 (Active mode), we want a real greeting. For Variants 1 and 3 (Analysis, Passive), we want the persona to **wait silently** until the visitor types first. But PROMISE doesn't have an "I want to wait" option; without a workaround, the persona would always greet first, ruining the design of Variants 1 and 3.
  - **What was changed:** No backend code was changed at all. The trick is purely in the **starter prompt** passed to the agent. In passive variants, the starter prompt reads: *"Respond with EXACTLY this text and nothing else: `__WAIT__`. No greeting, no explanation, no question, no emoji. Just the eight characters `__WAIT__` as your complete response."*
  - **How it works now:** GPT-4o obediently outputs the literal string `__WAIT__`. The backend stores this as a normal assistant message and returns it to the frontend. The frontend detects the `__WAIT__` token, **filters it out** of the chat display, and instead shows a hint like "Type to start the conversation". The agent stays in a valid PROMISE state — `currentState` is still active, `utterances` still has a (filtered) starter — ready to respond when the visitor finally writes.
  - **Why this is elegant:** No new state types, no new methods, no PROMISE modifications. Three different behaviours from one framework, all controlled by which prompt is loaded from `legacy_data` in Supabase.

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

**PROMISE adaptations — what was changed and why it works now:**

- **Context Compaction is essential here** (introduced in Step 5a):
  - Unlike the Biographer (where each block is a separate state and rarely reaches 20 messages), the Legacy Chat is a **single state** where every visitor message accumulates. Without compaction, a 100-message persona chat would re-send all 100 messages to GPT-4o on every new turn — tokens (and cost) growing linearly with conversation length.
  - With compaction, after message 21 the older history is summarised once and replaced. From that point on, only ~10 recent messages plus a short summary are sent — token usage stays roughly constant regardless of how long the chat runs.
  - **This is the place where compaction actually pays off in production**, far more than in the Biographer.

- **TTS Controller (new file):** [`TTSController.java`](src/main/java/ch/zhaw/statefulconversation/controllers/TTSController.java) — PROMISE has no audio output. Oblivio added this as a server-side bridge so the ElevenLabs API key stays on Railway (never exposed to the browser). Plus the new DTO [`TTSRequest.java`](src/main/java/ch/zhaw/statefulconversation/controllers/views/TTSRequest.java).

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

**PROMISE adaptations — what was changed and why it works now:**

- **Mode-scoped `visitor_id` (an Oblivio convention, no backend code change):**
  - **Why needed:** If the visitor switches from Variant 2 (where they've had a 10-message conversation) to Variant 1 (different persona behavior), should those 10 messages reappear, or should Variant 1 start fresh? Both are valid UX choices. Mixing them in one history makes the persona schizophrenic. Hard-separating them lets the visitor explore each variant cleanly.
  - **What was changed:** No backend code. The frontend's [`getScopedVisitorId(mode)`](Website-template/js/legacy-chat.js) appends `__active`, `__passive`, or `__analysis` to the base visitor UUID. Every read and write to `legacy_messages` uses the scoped ID.
  - **How it works now:** Three independent conversation histories per visitor + persona combination. The visitor can freely jump between them; messages don't bleed across modes. Variant switch creates a new PROMISE agent on the backend with the new prompt, but only the conversation history matching the new scope is loaded back.

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
