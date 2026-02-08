# Frontend Integration: Connecting Biographer.html to PROMISE

## 🎯 Overview

Diese Anleitung zeigt dir, wie du `biographer.html` modifizierst, um PROMISE anstelle der client-seitigen Fragen zu nutzen.

## 📋 Schritt 1: Supabase Tabellen erstellen

Gehe zu deinem Supabase Dashboard → SQL Editor und führe aus:

```bash
# Öffne die Datei und kopiere den Inhalt:
cat /Users/dennisriccardo/Bachelorarbeit_Oblivio/supabase_migrations.sql
```

Dies erstellt zwei neue Tabellen:
- `user_agents` - Speichert die PROMISE Agent-IDs pro User
- `user_legacies` - Speichert die fertigen Legacy-Stories

## 📋 Schritt 2: JavaScript einbinden

Füge in `biographer.html` **vor** dem schließenden `</body>` Tag hinzu:

```html
<!-- PROMISE API Integration -->
<script src="biographer-promise.js"></script>
```

## 📋 Schritt 3: Code-Änderungen in biographer.html

### 3.1 State-Variablen aktualisieren

Finde diese Zeilen (ca. Zeile 449):
```javascript
let currentUser = null;
let currentQuestionIndex = 0;
let conversationHistory = [];
```

Ersetze mit:
```javascript
let currentUser = null;
let currentAgentId = null;
let conversationHistory = [];
let isConversationActive = true;
```

### 3.2 getQuestions() Funktion entfernen

Lösche die gesamte `getQuestions()` Funktion (ca. Zeile 432-444):
```javascript
// Diese Funktion kann gelöscht werden - Fragen kommen jetzt von PROMISE
// function getQuestions() { ... }
```

### 3.3 initialize() Funktion aktualisieren

Finde die `initialize()` Funktion (ca. Zeile 500) und ersetze mit:

```javascript
async function initialize() {
    console.log('Initializing biographer with PROMISE...');

    const isAuthenticated = await checkAuth();
    if (!isAuthenticated) {
        console.log('Not authenticated, stopping initialization');
        return;
    }

    // Hide loading screen
    loadingScreen.classList.add('hidden');

    // Enable input
    messageInput.disabled = false;
    sendBtn.disabled = false;

    try {
        // Get current language
        const language = getCurrentLanguage();

        // Get or create PROMISE agent
        console.log('Getting PROMISE agent...');
        currentAgentId = await PromiseAPI.getOrCreateAgent(currentUser.id, language);
        console.log('Agent ID:', currentAgentId);

        // Load existing conversation if any
        await loadConversation();

        // If no previous conversation, start new one
        if (conversationHistory.length === 0) {
            console.log('Starting new conversation...');
            await startNewConversation();
        }

        console.log('Initialization complete!');
    } catch (error) {
        console.error('Error initializing:', error);
        addMessage('biographer', 'Sorry, there was an error starting the conversation. Please refresh the page.');
    }
}
```

### 3.4 loadConversation() Funktion aktualisieren

Ersetze die `loadConversation()` Funktion (ca. Zeile 540):

```javascript
async function loadConversation() {
    try {
        // Get conversation from PROMISE
        const conversation = await PromiseAPI.getConversation(currentAgentId);

        // Display messages
        if (conversation && conversation.utterances) {
            conversation.utterances.forEach(utterance => {
                const sender = utterance.speaker === 'ASSISTANT' ? 'biographer' : 'user';
                addMessageToUI(sender, utterance.text);
            });
            conversationHistory = conversation.utterances;
        }

        // Check if conversation is complete
        const agentInfo = await fetch(`http://localhost:8080/${currentAgentId}/info`);
        const agentData = await agentInfo.json();
        isConversationActive = agentData.active;

        if (!isConversationActive) {
            showCompletionMessage();
        }
    } catch (error) {
        console.log('No previous conversation found, starting fresh');
    }
}
```

### 3.5 askQuestion() Funktion LÖSCHEN und durch startNewConversation() ersetzen

Lösche `askQuestion()` und füge stattdessen hinzu:

```javascript
async function startNewConversation() {
    try {
        showTypingIndicator();

        // Start conversation with PROMISE
        const response = await PromiseAPI.startConversation(currentAgentId);

        hideTypingIndicator();
        addMessageToUI('biographer', response.text);

        isConversationActive = response.active;
    } catch (error) {
        hideTypingIndicator();
        console.error('Error starting conversation:', error);
        addMessageToUI('biographer', 'Sorry, I encountered an error. Please try again.');
    }
}
```

### 3.6 addMessage() Funktion umbenennen zu addMessageToUI()

Die existierende `addMessage()` Funktion (ca. Zeile 568) bleibt fast gleich, benenne sie nur um:

```javascript
function addMessageToUI(sender, text) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}`;

    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.textContent = sender === 'biographer' ? 'B' : currentUser?.user_metadata?.full_name?.[0] || 'U';

    const content = document.createElement('div');
    content.className = 'message-content';
    content.textContent = text;

    messageDiv.appendChild(avatar);
    messageDiv.appendChild(content);

    messagesArea.appendChild(messageDiv);
    messagesArea.scrollTop = messagesArea.scrollHeight;

    // Don't save to history here - PROMISE handles that
}
```

### 3.7 handleSendMessage() komplett neu schreiben

Ersetze die komplette `handleSendMessage()` Funktion (ca. Zeile 617):

```javascript
async function handleSendMessage() {
    const message = messageInput.value.trim();

    if (!message) return;

    if (!isConversationActive) {
        addMessageToUI('biographer', t('bio_complete'));
        return;
    }

    // Disable input while processing
    messageInput.disabled = true;
    sendBtn.disabled = true;

    // Add user message to UI
    addMessageToUI('user', message);
    messageInput.value = '';

    try {
        // Show typing indicator
        showTypingIndicator();

        // Send to PROMISE
        const response = await PromiseAPI.sendMessage(currentAgentId, message);

        // Hide typing indicator
        hideTypingIndicator();

        // Add PROMISE response
        addMessageToUI('biographer', response.text);

        // Update conversation status
        isConversationActive = response.active;

        // If conversation is complete
        if (!isConversationActive) {
            await handleConversationComplete();
        }

        // Re-enable input
        messageInput.disabled = false;
        sendBtn.disabled = false;
        messageInput.focus();
    } catch (error) {
        hideTypingIndicator();
        console.error('Error sending message:', error);
        addMessageToUI('biographer', 'Sorry, there was an error. Please try again.');

        // Re-enable input
        messageInput.disabled = false;
        sendBtn.disabled = false;
    }
}
```

### 3.8 Neue Funktion: handleConversationComplete()

Füge diese neue Funktion hinzu:

```javascript
async function handleConversationComplete() {
    console.log('Conversation complete! Getting legacy data...');

    try {
        // Get the complete legacy data from PROMISE
        const legacyData = await PromiseAPI.getLegacyData(currentAgentId);
        console.log('Legacy data:', legacyData);

        // Save to Supabase
        await PromiseAPI.saveLegacy(currentUser.id, currentAgentId, legacyData);

        // Show completion message
        showCompletionMessage();

        // Disable input
        messageInput.disabled = true;
        sendBtn.disabled = true;
    } catch (error) {
        console.error('Error handling conversation completion:', error);
    }
}

function showCompletionMessage() {
    const completionDiv = document.createElement('div');
    completionDiv.className = 'message biographer';
    completionDiv.innerHTML = `
        <div class="message-avatar">B</div>
        <div class="message-content">
            <strong>${t('bio_complete')}</strong><br><br>
            ${t('bio_complete_message') || 'Thank you for sharing your story with me. Your legacy has been saved and can be shared with your loved ones.'}
            <br><br>
            <a href="journey.html" style="color: var(--rust);">← ${t('nav_back') || 'Back to Journey'}</a>
        </div>
    `;
    messagesArea.appendChild(completionDiv);
    messagesArea.scrollTop = messagesArea.scrollHeight;
}
```

### 3.9 saveMessage() Funktion LÖSCHEN

Die `saveMessage()` Funktion (ca. Zeile 642) wird nicht mehr benötigt - PROMISE speichert alles:

```javascript
// Diese Funktion kann gelöscht werden
// async function saveMessage(questionIndex, answer) { ... }
```

### 3.10 getCurrentLanguage() Hilfsfunktion hinzufügen

Füge diese Funktion hinzu (sollte bereits in translations.js sein, falls nicht):

```javascript
function getCurrentLanguage() {
    return localStorage.getItem('selectedLanguage') || 'en';
}
```

## 📋 Schritt 4: CORS in PROMISE aktivieren

Da dein Frontend (`file://` oder `http://localhost`) mit PROMISE (`http://localhost:8080`) kommuniziert, brauchst du CORS.

Erstelle: `/Users/dennisriccardo/Bachelorarbeit_Oblivio/promise/src/main/java/ch/zhaw/statefulconversation/config/CorsConfig.java`

```java
package ch.zhaw.statefulconversation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}
```

Dann rebuild:
```bash
cd /Users/dennisriccardo/Bachelorarbeit_Oblivio/promise
mvn clean install -DskipTests
```

## 🧪 Schritt 5: Testen

1. **Start PROMISE**:
   ```bash
   cd /Users/dennisriccardo/Bachelorarbeit_Oblivio/promise
   mvn spring-boot:run
   ```

2. **Öffne biographer.html** in deinem Browser

3. **Log in** mit deinem Supabase Account

4. **Chat mit dem Biographer** - Die Fragen kommen jetzt von PROMISE!

## 🎯 Was passiert jetzt:

1. ✅ User loggt sich ein
2. ✅ System erstellt/holt PROMISE Agent für diesen User
3. ✅ PROMISE stellt automatisch Frage 1
4. ✅ User antwortet
5. ✅ PROMISE analysiert Antwort, speichert sie, und stellt Frage 2
6. ✅ ... wiederholt sich für alle 9 Fragen
7. ✅ Nach Frage 9: PROMISE generiert Legacy Summary
8. ✅ System speichert alles in Supabase
9. ✅ User sieht Completion-Nachricht

## 🐛 Debugging

- **PROMISE Console**: Zeigt alle Agent-Aktivitäten
- **Browser Console**: Zeigt Frontend-Logs
- **Monitor UI**: `http://localhost:8080/monitor/?agentId=YOUR_AGENT_ID`

## 📚 Wichtige Dateien

- ✅ `biographer.html` - Frontend (zu modifizieren)
- ✅ `biographer-promise.js` - PROMISE API Wrapper (neu erstellt)
- ✅ `supabase_migrations.sql` - Datenbank Schema (neu erstellt)
- ✅ `CorsConfig.java` - CORS Configuration (zu erstellen)

---

**Bereit für Integration? Sag Bescheid wenn du Hilfe bei einem Schritt brauchst!** 🚀
