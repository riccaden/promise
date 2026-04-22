-- ============================================
-- SUPABASE TABLES FOR OBLIVIO + PROMISE
-- ============================================
-- Dieses Script erstellt die Kerntabellen fuer die Oblivio-Plattform in Supabase.
-- Es definiert zwei Haupttabellen, Indizes, Row Level Security (RLS) Policies
-- und einen Trigger fuer automatische Zeitstempel-Aktualisierung.
-- Ausfuehrung: Im Supabase SQL Editor einfuegen und ausfuehren.

-- ============================================
-- Tabelle: user_agents
-- Speichert die Zuordnung zwischen Supabase-Benutzern und ihren PROMISE-Agenten.
-- Jeder Benutzer kann pro Sprache einen aktiven Agenten haben.
-- ============================================
CREATE TABLE IF NOT EXISTS user_agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),       -- Eindeutige Zeilen-ID
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,  -- Supabase Auth-Benutzer
    agent_id UUID NOT NULL,                              -- Referenz auf den PROMISE-Agenten (Spring Boot)
    language VARCHAR(5) DEFAULT 'en',                    -- Sprachcode (z.B. 'de', 'en', 'fr')
    is_active BOOLEAN DEFAULT true,                      -- Nur ein aktiver Agent pro Benutzer/Sprache
    created_at TIMESTAMPTZ DEFAULT NOW(),                -- Erstellungszeitpunkt
    updated_at TIMESTAMPTZ DEFAULT NOW(),                -- Letzte Aktualisierung (via Trigger)

    -- Constraint: Maximal ein aktiver Agent pro Benutzer und Sprache
    UNIQUE(user_id, language, is_active)
);

-- ============================================
-- Tabelle: user_legacies
-- Speichert abgeschlossene Legacy-Daten aus PROMISE-Konversationen.
-- legacy_data enthaelt alle Antworten und die Zusammenfassung als JSONB.
-- ============================================
CREATE TABLE IF NOT EXISTS user_legacies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),       -- Eindeutige Zeilen-ID
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,  -- Supabase Auth-Benutzer
    agent_id UUID NOT NULL,                              -- Zugehoeriger PROMISE-Agent
    legacy_data JSONB NOT NULL,                          -- Alle Antworten + Zusammenfassung als JSON
    created_at TIMESTAMPTZ DEFAULT NOW(),                -- Erstellungszeitpunkt
    completed_at TIMESTAMPTZ,                            -- Zeitpunkt der Fertigstellung (NULL = in Bearbeitung)
    is_published BOOLEAN DEFAULT false,                  -- Ob die Legacy veroeffentlicht wurde

    FOREIGN KEY (user_id, agent_id) REFERENCES user_agents(user_id, agent_id) ON DELETE CASCADE
);

-- Indizes fuer haeufige Abfragen (Performance-Optimierung)
CREATE INDEX IF NOT EXISTS idx_user_agents_user_id ON user_agents(user_id);
CREATE INDEX IF NOT EXISTS idx_user_agents_agent_id ON user_agents(agent_id);
CREATE INDEX IF NOT EXISTS idx_user_legacies_user_id ON user_legacies(user_id);
CREATE INDEX IF NOT EXISTS idx_user_legacies_created_at ON user_legacies(created_at DESC);

-- ============================================
-- Row Level Security (RLS)
-- Stellt sicher, dass Benutzer nur auf ihre eigenen Daten zugreifen koennen.
-- ============================================
ALTER TABLE user_agents ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_legacies ENABLE ROW LEVEL SECURITY;

-- RLS Policies fuer user_agents: Benutzer koennen nur eigene Agenten sehen/aendern
CREATE POLICY "Users can view their own agents"
    ON user_agents FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own agents"
    ON user_agents FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own agents"
    ON user_agents FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own agents"
    ON user_agents FOR DELETE
    USING (auth.uid() = user_id);

-- RLS Policies fuer user_legacies: Benutzer koennen nur eigene Legacies sehen/aendern
CREATE POLICY "Users can view their own legacies"
    ON user_legacies FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own legacies"
    ON user_legacies FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own legacies"
    ON user_legacies FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own legacies"
    ON user_legacies FOR DELETE
    USING (auth.uid() = user_id);

-- ============================================
-- Trigger-Funktion: Automatische Aktualisierung von updated_at
-- Wird bei jedem UPDATE auf user_agents aufgerufen.
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger: Setzt updated_at automatisch bei jeder Aenderung an user_agents
CREATE TRIGGER update_user_agents_updated_at
    BEFORE UPDATE ON user_agents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- DONE!
-- Dieses Script im Supabase SQL Editor ausfuehren.
-- ============================================
