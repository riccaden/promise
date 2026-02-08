# ⚡ Quick Start: Railway + Supabase Deployment

## 📝 Checkliste

### ☐ Schritt 1: Supabase (5 Minuten)
1. Gehe zu [supabase.com](https://supabase.com) → Sign In
2. **New Project** erstellen
3. **Settings** → **Database** → Connection String kopieren
4. Format: `postgresql://postgres.[project]:password@host:5432/postgres`

### ☐ Schritt 2: Railway (5 Minuten)
1. Gehe zu [railway.app](https://railway.app) → Sign In
2. **New Project** → **Deploy from GitHub**
3. Repository: `riccaden/promise` auswählen
4. Warte auf ersten Deploy

### ☐ Schritt 3: Environment Variables (2 Minuten)
Im Railway Dashboard → **Variables** → **Raw Editor**:

```bash
DATABASE_URL=postgresql://postgres.[IHR-PROJECT]:password@host:5432/postgres
OPENAI_KEY=sk-proj-...
```

### ☐ Schritt 4: Domain & Test (2 Minuten)
1. **Settings** → **Public Networking** → **Generate Domain**
2. Teste: `curl https://your-app.railway.app/actuator/health`

---

## 🎯 Erste API Calls

### Agent erstellen (mit User-ID):
```bash
curl -X POST https://your-app.railway.app/agent/singlestate \
  -H "Content-Type: application/json" \
  -d '{
    "type": 0,
    "userId": "user123",
    "agentName": "Test Agent",
    "agentDescription": "Mein erster Agent",
    "stateName": "Initial State",
    "statePrompt": "Du bist ein hilfreicher Assistent.",
    "stateStarterPrompt": "Beginne die Konversation freundlich.",
    "triggerToFinalPrompt": "Prüfe ob der User beenden möchte.",
    "guardToFinalPrompt": "Stelle sicher, dass alles geklärt ist.",
    "actionToFinalPrompt": "Erstelle eine Zusammenfassung."
  }'
```

### Conversation starten:
```bash
# Ersetze {agentId} mit der ID aus der vorherigen Antwort
curl -X POST https://your-app.railway.app/{agentId}/start
```

### User-Logs abrufen:
```bash
curl https://your-app.railway.app/user/user123/agents
curl https://your-app.railway.app/user/user123/conversations
curl https://your-app.railway.app/user/user123/stats
```

---

## 📖 Vollständige Dokumentation

Für Details siehe: [RAILWAY_SUPABASE_DEPLOYMENT.md](./RAILWAY_SUPABASE_DEPLOYMENT.md)

---

## 🚨 Häufige Probleme

| Problem | Lösung |
|---------|--------|
| `Connection refused` | DATABASE_URL prüfen |
| `Port already in use` | Railway setzt PORT automatisch |
| `OpenAI API error` | OPENAI_KEY prüfen |
| `404 Not Found` | Domain korrekt? Health-Check läuft? |

---

**Fertig in ~15 Minuten! 🎉**
