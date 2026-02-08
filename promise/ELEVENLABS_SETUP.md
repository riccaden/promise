# ElevenLabs Text-to-Speech Setup für PROMISE

## Übersicht

Die ElevenLabs Text-to-Speech Integration ist jetzt **sicher im Backend** implementiert. Der API-Schlüssel wird als Umgebungsvariable auf Railway gespeichert und ist nicht im Frontend sichtbar.

---

## ✅ Was wurde implementiert

### Backend (PROMISE):
1. **TTSController.java** - REST Endpoint für Text-to-Speech
2. **TTSRequest.java** - DTO für TTS Anfragen
3. Sicherer API-Call zu ElevenLabs von Backend aus

### Frontend (Website):
1. **biographer.html** - Angepasst, um Backend-Endpoint zu nutzen
2. **config.js** - ElevenLabs Konfiguration entfernt (jetzt im Backend)

---

## 🔧 Railway Environment Variables setzen

### Schritt 1: Railway Dashboard öffnen
1. Gehe zu: https://railway.app/dashboard
2. Wähle dein PROMISE Projekt aus

### Schritt 2: Variables hinzufügen
1. Klicke auf **Settings** (oder **Variables** Tab)
2. Füge folgende Environment Variables hinzu:

```
ELEVENLABS_API_KEY=sk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
ELEVENLABS_VOICE_ID=pNInz6obpgDQGcFmaJgB
```

**Wichtig:**
- `ELEVENLABS_API_KEY`: Dein ElevenLabs API-Schlüssel (beginnt mit `sk_`)
- `ELEVENLABS_VOICE_ID`: Voice ID von ElevenLabs (optional, Standard: Adam voice)

### Schritt 3: Redeploy
1. Klicke auf **Deploy** oder **Redeploy**
2. Warte bis Deployment abgeschlossen ist

---

## 🎤 ElevenLabs API Key bekommen

1. Gehe zu: https://elevenlabs.io/
2. Registriere dich für einen Account
3. Gehe zu **Profile → API Keys**
4. Erstelle einen neuen API Key
5. Kopiere den Key (beginnt mit `sk_`)

**Kostenlos:**
- 10,000 Zeichen pro Monat
- Ausreichend für Prototypen/Tests

---

## 🔍 Voice ID finden

### Standard Voice (Adam - empfohlen):
```
pNInz6obpgDQGcFmaJgB
```

### Andere Voices:
1. Gehe zu: https://elevenlabs.io/voice-library
2. Wähle eine Voice aus
3. Kopiere die Voice ID

**Beliebte Voices:**
- Adam (Male, calm): `pNInz6obpgDQGcFmaJgB`
- Rachel (Female, neutral): `21m00Tcm4TlvDq8ikWAM`
- Domi (Female, strong): `AZnzlk1XvdvUeBnXmlld`

---

## 🧪 Testen

1. Öffne: https://oblivio.ch/biographer.html
2. Starte eine Konversation
3. Der Biographer sollte sprechen (Voice Toggle muss aktiviert sein)

**Debug:**
- Browser Console öffnen (F12)
- Nach "TTS" Fehlern suchen
- Falls `503 Service Unavailable`: API Key nicht konfiguriert

---

## 📊 API Endpoint

### Request:
```
POST https://promise-production.up.railway.app/{agentID}/tts

Body:
{
  "text": "Hello, this is a test message."
}
```

### Response:
```
Content-Type: audio/mpeg
Body: <audio binary data>
```

### Status Codes:
- `200 OK`: Audio erfolgreich generiert
- `400 BAD REQUEST`: Kein Text angegeben
- `503 SERVICE UNAVAILABLE`: API Key nicht konfiguriert
- `500 INTERNAL SERVER ERROR`: ElevenLabs API Fehler

---

## 🔐 Sicherheit

✅ **Vorteile der Backend-Lösung:**
- API-Schlüssel ist nicht öffentlich sichtbar
- Keine Browser DevTools können den Key auslesen
- Zentrale Kontrolle über API-Nutzung
- Kann Rate Limiting implementieren

❌ **Alte Frontend-Lösung (entfernt):**
- API-Schlüssel war in config.js sichtbar
- Jeder konnte den Key kopieren und missbrauchen

---

## 💰 Kosten

**ElevenLabs Pricing:**
- **Free Tier**: 10,000 Zeichen/Monat (ca. 7-10 Minuten Audio)
- **Starter**: $5/Monat - 30,000 Zeichen
- **Creator**: $22/Monat - 100,000 Zeichen

**Durchschnittliche Nachricht:**
- 1 Biographer-Antwort ≈ 200 Zeichen
- 10,000 Zeichen ≈ 50 Nachrichten

---

## 📝 Code-Struktur

```
promise/
├── src/main/java/ch/zhaw/statefulconversation/
│   └── controllers/
│       ├── TTSController.java          ← Neuer TTS Endpoint
│       └── views/
│           └── TTSRequest.java         ← DTO für TTS Request

Website/
├── biographer.html                     ← Frontend ruft Backend auf
└── js/
    └── config.js                       ← API Key entfernt
```

---

## ❓ Troubleshooting

### Problem: "ElevenLabs API key not configured on backend"
**Lösung:**
1. Prüfe Railway Environment Variables
2. Stelle sicher dass `ELEVENLABS_API_KEY` gesetzt ist
3. Redeploy PROMISE

### Problem: Voice spielt nicht ab
**Lösung:**
1. Prüfe Browser Console (F12)
2. Voice Toggle Button aktiviert?
3. Browser erlaubt Audio Autoplay?

### Problem: 503 Service Unavailable
**Lösung:**
- Backend kann ElevenLabs nicht erreichen
- Prüfe Railway Logs
- Prüfe API Key Gültigkeit

---

## 🎉 Fertig!

Deine ElevenLabs Integration ist jetzt **sicher und produktionsbereit**! 🚀
