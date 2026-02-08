# 👥 User-Tracking Features

## Übersicht

Das PROMISE Framework wurde erweitert um **Multi-User-Tracking** zu unterstützen. Jeder Agent kann nun einer spezifischen User-ID zugeordnet werden, und alle Conversations können user-spezifisch abgerufen werden.

## 🎯 Features

### 1. User-ID pro Agent
- Jeder Agent kann eine `userId` haben
- Beim Erstellen eines Agents kann die `userId` mitgegeben werden
- Agents ohne `userId` sind weiterhin möglich (abwärtskompatibel)

### 2. User-spezifische API Endpoints
Neue REST-Endpoints unter `/user/{userId}/`:
- Liste aller Agents eines Users
- Alle Conversations eines Users (über alle Agents)
- Conversations seit einem bestimmten Zeitpunkt
- Statistiken pro User

## 📡 API Endpoints

### Alle Agents eines Users abrufen
```http
GET /user/{userId}/agents
```

**Response:**
```json
[
  {
    "id": "3f2b8c6a-4b5a-4b3f-a6f0-1d3e7c6d4e1a",
    "name": "Digital Companion",
    "description": "Daily check-in conversation",
    "active": true,
    "conversationCount": 12
  }
]
```

---

### Alle Conversations eines Users abrufen
```http
GET /user/{userId}/conversations
```

**Optional Query Parameter:**
- `since` - Unix timestamp (milliseconds) für Filterung

**Beispiel:**
```http
GET /user/user123/conversations?since=1704067200000
```

**Response:**
```json
[
  {
    "agentId": "3f2b8c6a-4b5a-4b3f-a6f0-1d3e7c6d4e1a",
    "agentName": "Digital Companion",
    "role": "user",
    "content": "I am feeling well today",
    "stateName": "Check-In Interaction",
    "timestamp": "2024-01-01T10:30:00Z"
  },
  {
    "agentId": "3f2b8c6a-4b5a-4b3f-a6f0-1d3e7c6d4e1a",
    "agentName": "Digital Companion",
    "role": "assistant",
    "content": "That's wonderful to hear!",
    "stateName": "Check-In Interaction",
    "timestamp": "2024-01-01T10:30:05Z"
  }
]
```

---

### Conversation eines spezifischen Agents abrufen
```http
GET /user/{userId}/agent/{agentId}/conversation
```

**Security:** Prüft automatisch, ob der Agent dem User gehört (403 Forbidden wenn nicht)

**Response:** Gleiche Struktur wie `/conversations`, aber nur für einen Agent

---

### User-Statistiken abrufen
```http
GET /user/{userId}/stats
```

**Response:**
```json
{
  "userId": "user123",
  "totalAgents": 5,
  "activeAgents": 2,
  "totalConversations": 47
}
```

## 🔧 Agent mit User-ID erstellen

### Beim Erstellen eines Agents

Erweitere das JSON um das `userId` Feld:

```bash
curl -X POST http://localhost:8080/agent/singlestate \
  -H "Content-Type: application/json" \
  -d '{
    "type": 0,
    "userId": "user123",
    "agentName": "Digital Companion",
    "agentDescription": "Daily check-in conversation.",
    ...
  }'
```

### Programmatisch

```java
Agent agent = new Agent("Digital Companion", "Daily check-in", initialState);
agent.setUserId("user123");
repository.save(agent);
```

## 💡 Use Cases

### 1. Therapie-App mit mehreren Patienten
```javascript
// Patient startet neue Session
const response = await fetch('/agent/singlestate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    userId: patientId,
    agentName: 'Therapy Session',
    ...
  })
});

// Arzt ruft Patientenhistorie ab
const history = await fetch(`/user/${patientId}/conversations`);
```

### 2. Multi-Tenant Plattform
```javascript
// Pro Kunde separate Agents
const createAgentForCustomer = async (customerId) => {
  return fetch('/agent/singlestate', {
    method: 'POST',
    body: JSON.stringify({
      userId: customerId,
      ...
    })
  });
};

// Alle Agents eines Kunden anzeigen
const getCustomerAgents = async (customerId) => {
  return fetch(`/user/${customerId}/agents`);
};
```

### 3. Analytics & Monitoring
```javascript
// User-Engagement tracken
const getEngagementStats = async (userId) => {
  const stats = await fetch(`/user/${userId}/stats`);
  console.log(`User ${userId}:
    - Total Agents: ${stats.totalAgents}
    - Active Sessions: ${stats.activeAgents}
    - Total Messages: ${stats.totalConversations}
  `);
};

// Conversations der letzten 24h
const yesterday = Date.now() - (24 * 60 * 60 * 1000);
const recent = await fetch(`/user/${userId}/conversations?since=${yesterday}`);
```

## 🔐 Sicherheit & Zugriffskontrolle

### Aktueller Stand
- Basis-Validierung: Prüfung ob Agent dem User gehört
- `403 Forbidden` bei unautorisierten Zugriffen

### Empfohlene Erweiterungen
Für Produktions-Umgebungen:

1. **Authentication Layer hinzufügen:**
```java
@RestController
@RequestMapping("/user")
public class UserLogController {

    @GetMapping("/{userId}/agents")
    public ResponseEntity<?> getUserAgents(
        @PathVariable String userId,
        @AuthenticationPrincipal UserDetails currentUser) {

        // Validiere dass currentUser auf userId zugreifen darf
        if (!authService.canAccess(currentUser, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ...
    }
}
```

2. **JWT Token Integration:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

3. **Role-Based Access Control:**
```java
@PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#userId)")
@GetMapping("/user/{userId}/conversations")
public ResponseEntity<?> getUserConversations(@PathVariable String userId) {
    ...
}
```

## 📊 Datenbank-Schema

### Agent Table
```sql
CREATE TABLE agent (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    user_id VARCHAR(255),  -- NEU!
    -- weitere Felder...
);

-- Index für schnelle User-Queries
CREATE INDEX idx_agent_user_id ON agent(user_id);
```

### Utterance Table (unverändert)
```sql
CREATE TABLE utterance (
    id UUID PRIMARY KEY,
    role VARCHAR(50),
    content TEXT,
    created_date TIMESTAMP,
    state_name VARCHAR(255),
    -- Relation zu Agent über Utterances
);
```

## 🧪 Testing

### Unit Tests
```java
@Test
public void testUserAgentFiltering() {
    Agent agent1 = new Agent("Agent 1", "Description", state);
    agent1.setUserId("user123");

    Agent agent2 = new Agent("Agent 2", "Description", state);
    agent2.setUserId("user456");

    repository.saveAll(List.of(agent1, agent2));

    List<Agent> user123Agents = repository.findAll().stream()
        .filter(a -> "user123".equals(a.getUserId()))
        .toList();

    assertEquals(1, user123Agents.size());
    assertEquals("Agent 1", user123Agents.get(0).getName());
}
```

### Integration Tests
```java
@Test
public void testUserConversationsEndpoint() {
    mockMvc.perform(get("/user/user123/conversations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].agentId").exists())
        .andExpect(jsonPath("$[0].timestamp").exists());
}
```

## 🔄 Migration von bestehenden Daten

Falls du bereits Agents in der Datenbank hast:

```sql
-- Optional: Setze Default User für alte Agents
UPDATE agent
SET user_id = 'legacy-user'
WHERE user_id IS NULL;

-- Oder: Assign basierend auf Agent-Namen
UPDATE agent
SET user_id = 'patient-' || substring(name from 1 for 8)
WHERE user_id IS NULL AND name LIKE 'Patient%';
```

## 📈 Performance-Optimierungen

### Database Indexes
```sql
-- Für schnelle User-Queries
CREATE INDEX idx_agent_user_id ON agent(user_id);

-- Für Zeitbasierte Queries
CREATE INDEX idx_utterance_created_date ON utterance(created_date);

-- Composite Index für User + Zeit
CREATE INDEX idx_agent_user_active
ON agent(user_id, id)
WHERE user_id IS NOT NULL;
```

### Caching (Optional)
```java
@Cacheable(value = "userStats", key = "#userId")
public UserStatsView getUserStats(String userId) {
    // Expensive operation
    return calculateStats(userId);
}
```

## 🚀 Deployment

User-Tracking funktioniert out-of-the-box nach Deployment auf:
- ✅ Railway
- ✅ Supabase (PostgreSQL)
- ✅ Heroku
- ✅ AWS
- ✅ Jede PostgreSQL-basierte Umgebung

Siehe: [RAILWAY_SUPABASE_DEPLOYMENT.md](./RAILWAY_SUPABASE_DEPLOYMENT.md)

## 📚 Weitere Informationen

- [PROMISE README](./README.md)
- [Railway Deployment Guide](./RAILWAY_SUPABASE_DEPLOYMENT.md)
- [Quick Start Guide](./QUICKSTART.md)
