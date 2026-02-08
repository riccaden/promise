# 🚀 Railway + Supabase Deployment Guide

Dieses Dokument beschreibt, wie du das PROMISE Framework auf **Railway** mit einer **Supabase PostgreSQL** Datenbank deployest.

## 📋 Voraussetzungen

- [Railway Account](https://railway.app/) (kostenlos)
- [Supabase Account](https://supabase.com/) (kostenlos)
- [GitHub Account](https://github.com/)
- OpenAI API Key

## 🗄️ Schritt 1: Supabase Datenbank einrichten

### 1.1 Projekt erstellen
1. Gehe zu [Supabase Dashboard](https://app.supabase.com/)
2. Klicke auf **"New Project"**
3. Wähle einen Projektnamen (z.B. `promise-db`)
4. Wähle ein sicheres Passwort für die Datenbank
5. Wähle eine Region (Europa empfohlen)
6. Klicke auf **"Create new project"**

### 1.2 Connection String abrufen
1. Gehe zu **Settings** → **Database**
2. Scrolle zu **Connection string** → **URI**
3. Kopiere die Connection String (Format: `postgresql://postgres:[YOUR-PASSWORD]@[HOST]:[PORT]/postgres`)
4. Ersetze `[YOUR-PASSWORD]` mit deinem Datenbankpasswort

**Beispiel:**
```
postgresql://postgres.abcdefghijklmnop:yourpassword@aws-0-eu-central-1.pooler.supabase.com:5432/postgres
```

> ⚠️ **Wichtig:** Bewahre diese Connection String sicher auf!

## 🚂 Schritt 2: Railway Projekt einrichten

### 2.1 Neues Projekt erstellen
1. Gehe zu [Railway Dashboard](https://railway.app/)
2. Klicke auf **"New Project"**
3. Wähle **"Deploy from GitHub repo"**
4. Autorisiere Railway mit GitHub
5. Wähle dein Repository: `riccaden/promise`

### 2.2 Environment Variables konfigurieren

Klicke auf dein Projekt → **Variables** → **Raw Editor** und füge folgende Environment Variables hinzu:

```bash
# Supabase Database Connection
DATABASE_URL=postgresql://postgres.[YOUR-PROJECT]:yourpassword@aws-0-eu-central-1.pooler.supabase.com:5432/postgres

# Alternativ: Einzelne Connection Details
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.[YOUR-PROJECT]
SPRING_DATASOURCE_PASSWORD=yourpassword

# OpenAI API Key
OPENAI_KEY=sk-proj-...

# Railway setzt PORT automatisch (optional)
# PORT=8080
```

> 💡 **Tipp:** Verwende entweder `DATABASE_URL` ODER die einzelnen `SPRING_DATASOURCE_*` Variablen.

### 2.3 Deployment starten

Railway deployed automatisch nach dem Push zu GitHub. Du kannst den Build-Status im **Deployments** Tab verfolgen.

## 🧪 Schritt 3: Deployment testen

### 3.1 URL abrufen
1. Gehe zu **Settings** → **Public Networking**
2. Klicke auf **"Generate Domain"**
3. Kopiere die generierte URL (z.B. `https://your-app.railway.app`)

### 3.2 Health Check
Teste ob die Anwendung läuft:
```bash
curl https://your-app.railway.app/actuator/health
```

**Erwartete Antwort:**
```json
{
  "status": "UP"
}
```

### 3.3 Agents abrufen
```bash
curl https://your-app.railway.app/agent
```

## 👥 Schritt 4: User-Tracking nutzen

### 4.1 Agent mit User-ID erstellen

Erstelle einen Agent mit einer spezifischen User-ID:

```bash
curl -X POST https://your-app.railway.app/agent/singlestate \
  -H "Content-Type: application/json" \
  -d '{
    "type": 0,
    "userId": "user123",
    "agentName": "Digital Companion",
    "agentDescription": "Daily check-in conversation.",
    "stateName": "Check-In Interaction",
    "statePrompt": "As a digital therapy coach, conduct daily check-ins...",
    "stateStarterPrompt": "Compose a single, very short message...",
    "triggerToFinalPrompt": "Review the patient latest messages...",
    "guardToFinalPrompt": "Review the conversation to confirm...",
    "actionToFinalPrompt": "Summarize the coach-patient conversation..."
  }'
```

### 4.2 User-spezifische Logs abrufen

#### Alle Agents eines Users:
```bash
curl https://your-app.railway.app/user/user123/agents
```

#### Alle Conversations eines Users:
```bash
curl https://your-app.railway.app/user/user123/conversations
```

#### Conversations seit einem bestimmten Zeitpunkt:
```bash
curl "https://your-app.railway.app/user/user123/conversations?since=1704067200000"
```

#### Conversation eines spezifischen Agents:
```bash
curl https://your-app.railway.app/user/user123/agent/{agentId}/conversation
```

#### User-Statistiken:
```bash
curl https://your-app.railway.app/user/user123/stats
```

**Antwort-Beispiel:**
```json
{
  "userId": "user123",
  "totalAgents": 5,
  "activeAgents": 2,
  "totalConversations": 47
}
```

## 🔧 Schritt 5: Konfiguration & Monitoring

### 5.1 Logs anzeigen
Im Railway Dashboard → **Logs** Tab kannst du Live-Logs sehen.

### 5.2 Datenbank-Zugriff
In Supabase → **Table Editor** kannst du die Datenbank direkt verwalten.

### 5.3 Auto-Scaling
Railway skaliert automatisch basierend auf dem Traffic.

## 📊 API Endpoints Übersicht

### Standard Endpoints (aus README.md)
- `GET /{agentID}/info` - Agent-Info
- `GET /{agentID}/conversation` - Conversation History
- `POST /{agentID}/start` - Conversation starten
- `POST /{agentID}/respond` - Antworten senden
- `DELETE /{agentID}/reset` - Conversation zurücksetzen

### Neue User-Tracking Endpoints
- `GET /user/{userId}/agents` - Alle Agents eines Users
- `GET /user/{userId}/conversations` - Alle Conversations eines Users
- `GET /user/{userId}/agent/{agentId}/conversation` - Spezifische Conversation
- `GET /user/{userId}/stats` - User-Statistiken

### Log-Streaming
- `GET /logs/stream` - Server-Sent Events (SSE) Log Stream

## 🔐 Sicherheit

### Environment Variables
- **Nie** API Keys oder Passwörter im Code committen
- Verwende Railway's **Environment Variables** für sensitive Daten
- Rotiere API Keys regelmäßig

### CORS (Optional)
Falls du das Backend von einem Frontend aus anderer Domain aufrufst, füge CORS-Konfiguration hinzu:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://your-frontend.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

## 🐛 Troubleshooting

### Problem: Deployment schlägt fehl
**Lösung:**
- Prüfe Railway Logs: Dashboard → **Logs**
- Stelle sicher, dass alle Environment Variables korrekt gesetzt sind
- Prüfe ob `Dockerfile` korrekt ist

### Problem: Database Connection Error
**Lösung:**
- Prüfe `DATABASE_URL` Format
- Stelle sicher, dass das Datenbankpasswort korrekt ist
- Prüfe Supabase Dashboard → **Database** → **Connection Pooling** ist aktiviert

### Problem: Application startet nicht (Port Error)
**Lösung:**
- Railway setzt `PORT` automatisch
- Stelle sicher, dass in `application-prod.properties` `server.port=${PORT:8080}` verwendet wird

### Problem: Out of Memory
**Lösung:**
- Erhöhe Memory Limit in Railway: Settings → **Resources**
- Optimiere Hibernate Connection Pool in `application-prod.properties`

## 📈 Nächste Schritte

1. **Frontend Integration:** Integriere die PROMISE API mit deinem Frontend
2. **Authentication:** Implementiere User-Authentication (z.B. JWT)
3. **Monitoring:** Füge Application Monitoring hinzu (z.B. Sentry)
4. **Custom Domain:** Verbinde eine Custom Domain in Railway Settings

## 📚 Weitere Ressourcen

- [Railway Dokumentation](https://docs.railway.app/)
- [Supabase Dokumentation](https://supabase.com/docs)
- [PROMISE README](./README.md)
- [Spring Boot Deployment Guide](https://spring.io/guides/gs/spring-boot-docker/)

## 💬 Support

Bei Fragen oder Problemen:
- Railway Support: [Railway Discord](https://discord.gg/railway)
- Supabase Support: [Supabase Discord](https://discord.supabase.com/)
- PROMISE Issues: [GitHub Issues](https://github.com/riccaden/promise/issues)

---

**🎉 Viel Erfolg mit deinem Deployment!**
