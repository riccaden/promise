# PROMISE Integration Guide for Oblivio Biographer

## ✅ What's Been Completed

### Phase 1: PROMISE Backend Setup ✓
1. ✅ Cloned PROMISE repository
2. ✅ Created MySQL database `oblivio_promise`
3. ✅ Configured database connection
4. ✅ Built PROMISE with Maven
5. ✅ Created custom Biographer agent with 9-question flow

### Custom Biographer Agent Features:
- **Multi-state conversation flow**: Each question is a separate state
- **Bilingual support**: English and German
- **Automatic progression**: Moves to next question after meaningful response
- **Data extraction**: Stores each answer in PROMISE storage
- **Legacy summary**: Generates comprehensive life story at the end

## 🔧 Required: Add Your OpenAI API Key

Before starting PROMISE, you need to add your OpenAI API key:

1. Open the configuration file:
   ```bash
   nano /Users/dennisriccardo/Bachelorarbeit_Oblivio/promise/src/main/resources/openai.properties
   ```

2. Replace `YOUR_OPENAI_API_KEY_HERE` with your actual OpenAI API key:
   ```properties
   openai.key = sk-your-actual-api-key-here
   ```

3. Save and close (Ctrl+X, then Y, then Enter)

## 🚀 Starting the PROMISE Backend

Start the PROMISE server:

```bash
cd /Users/dennisriccardo/Bachelorarbeit_Oblivio/promise
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

## 📝 Creating a Biographer Agent

Once the server is running, create a Biographer agent via REST API:

### English Biographer:
```bash
curl -X POST http://localhost:8080/agent/biographer \
  -H "Content-Type: application/json" \
  -d '{
    "type": 2,
    "agentName": "Biographer",
    "agentDescription": "Guides users through creating their digital legacy",
    "language": "en"
  }'
```

### German Biographer:
```bash
curl -X POST http://localhost:8080/agent/biographer \
  -H "Content-Type: application/json" \
  -d '{
    "type": 2,
    "agentName": "Biograf",
    "agentDescription": "Führt Benutzer durch die Erstellung ihres digitalen Erbes",
    "language": "de"
  }'
```

**Response Example:**
```json
{
  "id": "3f2b8c6a-4b5a-4b3f-a6f0-1d3e7c6d4e1a",
  "name": "Biographer",
  "description": "Guides users through creating their digital legacy",
  "active": false
}
```

**Save the `id` - you'll need it for the frontend integration!**

## 🔗 PROMISE API Endpoints

### Start Conversation:
```bash
POST http://localhost:8080/{agentID}/start
```

### Send User Response:
```bash
POST http://localhost:8080/{agentID}/respond
Content-Type: text/plain

[User's answer to the current question]
```

### Get Conversation History:
```bash
GET http://localhost:8080/{agentID}/conversation
```

### Get Current State:
```bash
GET http://localhost:8080/{agentID}/state
```

### Get Stored Legacy Data:
```bash
GET http://localhost:8080/{agentID}/storage
```

## 🌐 Frontend Integration (Next Steps)

### Current Setup:
- **Frontend**: `/Users/dennisriccardo/Bachelorarbeit_Oblivio/Website/biographer.html`
- **Backend**: PROMISE at `http://localhost:8080`

### Required Changes to `biographer.html`:

1. **Remove client-side question logic** (currently at lines ~565-620)
2. **Add PROMISE API integration**:

```javascript
// Configuration
const PROMISE_URL = 'http://localhost:8080';
let currentAgentId = null;

// Initialize conversation
async function initializeBiographer() {
    // Create or retrieve agent ID
    currentAgentId = localStorage.getItem('biographer_agent_id');

    if (!currentAgentId) {
        // Create new biographer agent
        const lang = getCurrentLanguage(); // from translations.js
        const response = await fetch(`${PROMISE_URL}/agent/biographer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                type: 2,
                agentName: 'Biographer',
                agentDescription: 'Guides users through creating their digital legacy',
                language: lang
            })
        });
        const data = await response.json();
        currentAgentId = data.id;
        localStorage.setItem('biographer_agent_id', currentAgentId);
    }

    // Start conversation
    const startResponse = await fetch(`${PROMISE_URL}/${currentAgentId}/start`, {
        method: 'POST'
    });
    const startData = await startResponse.json();

    // Display first question
    addMessage(startData.assistantResponse.text, 'bot');
}

// Send user answer
async function sendAnswer(answer) {
    const response = await fetch(`${PROMISE_URL}/${currentAgentId}/respond`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: answer
    });
    const data = await response.json();

    if (data.active) {
        // More questions remaining
        addMessage(data.assistantResponse.text, 'bot');
    } else {
        // Conversation complete
        showCompletionMessage();

        // Retrieve legacy summary
        const storageResponse = await fetch(`${PROMISE_URL}/${currentAgentId}/storage`);
        const storageData = await storageResponse.json();
        console.log('Legacy Summary:', storageData);
    }
}
```

## 📊 The 9 Biographer Questions

### English:
1. What's your full name?
2. Tell me about your childhood. Where did you grow up, and what are your fondest memories?
3. What are the most important values in your life? What principles guide your decisions?
4. Who has had the biggest influence on your life? How did they shape who you are today?
5. What accomplishments are you most proud of in your life?
6. What lessons have you learned that you'd like to pass on to future generations?
7. If you could give one piece of advice to your loved ones, what would it be?
8. What brings you the most joy in life?
9. Is there anything else you'd like to share about your life story?

### German (Deutsch):
1. Wie ist dein vollständiger Name?
2. Erzähle mir von deiner Kindheit. Wo bist du aufgewachsen und was sind deine liebsten Erinnerungen?
3. Was sind die wichtigsten Werte in deinem Leben? Welche Prinzipien leiten deine Entscheidungen?
4. Wer hatte den größten Einfluss auf dein Leben? Wie haben sie dich zu dem gemacht, der du heute bist?
5. Auf welche Leistungen in deinem Leben bist du am stolzesten?
6. Welche Lektionen hast du gelernt, die du an zukünftige Generationen weitergeben möchtest?
7. Wenn du deinen Lieben einen Ratschlag geben könntest, welcher wäre es?
8. Was bringt dir im Leben die größte Freude?
9. Gibt es noch etwas, das du über deine Lebensgeschichte teilen möchtest?

## 📦 Data Storage

PROMISE stores each answer with a key:
- `answer_q1`: Name
- `answer_q2`: Childhood memories
- `answer_q3`: Values
- `answer_q4`: Influences
- `answer_q5`: Accomplishments
- `answer_q6`: Lessons
- `answer_q7`: Advice
- `answer_q8`: Joy
- `answer_q9`: Additional insights
- `legacy_summary`: Complete life story summary (generated at the end)

## 🔄 Connecting to Supabase

After collecting the legacy data from PROMISE, store it in Supabase:

```javascript
// After conversation completes
async function saveLegacyToSupabase() {
    const storageResponse = await fetch(`${PROMISE_URL}/${currentAgentId}/storage`);
    const legacyData = await storageResponse.json();

    const { data: { user } } = await supabaseClient.auth.getUser();

    // Store in Supabase
    const { error } = await supabaseClient
        .from('user_legacies')
        .insert({
            user_id: user.id,
            legacy_data: legacyData,
            created_at: new Date().toISOString()
        });

    if (error) console.error('Error saving legacy:', error);
}
```

## 🧪 Testing the Integration

1. **Start PROMISE**: `cd promise && mvn spring-boot:run`
2. **Create Agent**: Use the curl command above
3. **Test in Browser**: Visit `http://localhost:8080/?agentId=YOUR_AGENT_ID`
4. **Or use Monitor**: `http://localhost:8080/monitor/?agentId=YOUR_AGENT_ID` (see state transitions live!)

## 🎯 Next Steps Roadmap

### Phase 2: Frontend Integration (Todo)
1. ☐ Modify `biographer.html` to use PROMISE API instead of client-side questions
2. ☐ Handle authentication with Supabase before creating agent
3. ☐ Store agent ID per user session
4. ☐ Display conversation from PROMISE
5. ☐ Handle language switching (create new agent with different language)

### Phase 3: Data Persistence (Todo)
6. ☐ Create Supabase table `user_legacies` to store completed legacies
7. ☐ Save PROMISE storage data to Supabase when conversation completes
8. ☐ Allow users to resume incomplete conversations
9. ☐ Export legacy as PDF or shareable format

### Phase 4: Advanced Features (Future)
10. ☐ Allow loved ones to interact with completed legacy (chat with the person's story)
11. ☐ Add photo/video upload capability
12. ☐ Implement time capsule messages
13. ☐ Create shareable legacy pages

## 🐛 Troubleshooting

### Build Errors:
```bash
cd /Users/dennisriccardo/Bachelorarbeit_Oblivio/promise
mvn clean install -DskipTests
```

### MySQL Connection Issues:
Check your MySQL is running:
```bash
mysql -u root -e "SHOW DATABASES;"
```

### OpenAI API Issues:
- Verify your API key is valid
- Check you have credits available at https://platform.openai.com/usage
- Model used: `gpt-4o` (configured in `openai.properties`)

### CORS Issues (Frontend → Backend):
If you encounter CORS errors when calling PROMISE from your frontend, you may need to add CORS configuration to PROMISE. Let me know and I can help with that.

## 📚 Resources

- **PROMISE Documentation**: `/Users/dennisriccardo/Bachelorarbeit_Oblivio/promise/README.md`
- **PROMISE API**: Fully documented in the README
- **Your Website**: `/Users/dennisriccardo/Bachelorarbeit_Oblivio/Website/`

## ✨ What Makes This Special

Your Biographer now uses:
- ✅ **State Machine Architecture**: Professional conversational AI framework
- ✅ **LLM-Powered Responses**: Natural, empathetic conversations
- ✅ **Structured Data Collection**: Each answer is extracted and stored
- ✅ **Bilingual Support**: Works in English and German
- ✅ **Scalable**: Can easily add more questions or modify the flow
- ✅ **Production-Ready**: Built on enterprise-grade framework (Spring Boot + JPA + MySQL)

---

**Need help with the next steps? Just let me know which phase you'd like to tackle next!**
