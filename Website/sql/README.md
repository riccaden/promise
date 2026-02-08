# Supabase Database Setup

## Chat Messages Table

Um die persistente Speicherung von Chatverläufen zu aktivieren, muss die `chat_messages` Tabelle in Supabase erstellt werden.

### Anleitung:

1. Gehe zu deinem Supabase Dashboard: https://supabase.com/dashboard
2. Wähle dein Projekt aus (`gmpgsozqcadbofezzloo`)
3. Gehe zu **SQL Editor** im linken Menü
4. Öffne die Datei `create_chat_messages.sql`
5. Kopiere den gesamten SQL-Code
6. Füge ihn in den SQL Editor ein
7. Klicke auf **Run** (oder drücke `Cmd+Enter`)

### Was wird erstellt:

- **Tabelle `chat_messages`**: Speichert alle Chat-Nachrichten
  - `id`: Eindeutige ID
  - `user_id`: Verknüpfung zum User
  - `agent_id`: Welcher Agent (Biographer)
  - `role`: "user" oder "assistant"
  - `content`: Der Nachrichteninhalt
  - `created_at`: Zeitstempel

- **Row Level Security (RLS)**: Nur der jeweilige User kann seine eigenen Nachrichten sehen
- **Policies**: Automatische Zugriffskontrolle

### Funktionsweise:

Nach dem Setup werden alle Chat-Nachrichten automatisch:
1. In Supabase gespeichert (persistent)
2. Beim nächsten Besuch wieder geladen
3. Auch wenn das PROMISE Backend offline ist verfügbar

Die Chatverläufe bleiben dauerhaft erhalten! 🎉
