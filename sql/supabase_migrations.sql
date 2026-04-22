-- ============================================
-- SUPABASE MIGRATIONS FOR PROMISE INTEGRATION
-- ============================================
-- Migrations-Script fuer die Oblivio-Plattform.
-- Erstellt Tabellen, RLS Policies, Indizes und Trigger in Supabase.
-- Unterschied zu SUPABASE_TABLES.sql: Enthaelt zusaetzlich Share-Funktionalitaet
-- (is_shared, share_token) und erlaubt oeffentlichen Lesezugriff auf geteilte Legacies.
-- Ausfuehrung: Im Supabase SQL Editor einfuegen und ausfuehren.

-- ============================================
-- Migration 1: Tabelle user_agents
-- Speichert die Zuordnung zwischen Supabase-Benutzern und PROMISE-Agenten.
-- Pro Benutzer und Sprache existiert maximal ein Agent (UNIQUE Constraint).
-- ============================================
CREATE TABLE IF NOT EXISTS user_agents (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,       -- Eindeutige Zeilen-ID
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,  -- Supabase Auth-Benutzer
    agent_id UUID NOT NULL,                              -- PROMISE-Agent-ID aus Spring Boot
    language VARCHAR(10) NOT NULL DEFAULT 'en',          -- Sprachcode (z.B. 'de', 'en', 'fr', 'it')
    is_active BOOLEAN DEFAULT true,                      -- Aktiv-Status des Agenten
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- Erstellungszeitpunkt
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- Letzte Aktualisierung (via Trigger)
    UNIQUE(user_id, language)                            -- Ein Agent pro Benutzer und Sprache
);

-- ============================================
-- Migration 2: Tabelle user_legacies
-- Speichert abgeschlossene Legacies mit optionaler Share-Funktionalitaet.
-- legacy_data enthaelt alle PROMISE-Antworten und Zusammenfassung als JSONB.
-- ============================================
CREATE TABLE IF NOT EXISTS user_legacies (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,       -- Eindeutige Zeilen-ID
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,  -- Supabase Auth-Benutzer
    agent_id UUID NOT NULL,                              -- Zugehoeriger PROMISE-Agent
    legacy_data JSONB NOT NULL,                          -- Alle Antworten + Zusammenfassung als JSON
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- Erstellungszeitpunkt
    completed_at TIMESTAMP WITH TIME ZONE,               -- Fertigstellungszeitpunkt (NULL = in Bearbeitung)
    is_shared BOOLEAN DEFAULT false,                     -- Ob die Legacy oeffentlich geteilt wurde
    share_token UUID DEFAULT gen_random_uuid()           -- Eindeutiger Token fuer oeffentlichen Zugriff
);

-- ============================================
-- Migration 3: Row Level Security (RLS) aktivieren
-- Schuetzt Daten auf Datenbankebene – Benutzer sehen nur eigene Eintraege.
-- ============================================
ALTER TABLE user_agents ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_legacies ENABLE ROW LEVEL SECURITY;

-- RLS Policies fuer user_agents: Nur eigene Agenten sichtbar/aenderbar
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

-- RLS Policies fuer user_legacies: Eigene Legacies + geteilte Legacies lesbar
CREATE POLICY "Users can view their own legacies"
    ON user_legacies FOR SELECT
    USING (auth.uid() = user_id OR is_shared = true);

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
-- Migration 4: Indizes fuer Performance-Optimierung
-- ============================================
CREATE INDEX idx_user_agents_user_id ON user_agents(user_id);       -- Schnelle Suche nach Benutzer
CREATE INDEX idx_user_agents_language ON user_agents(language);      -- Schnelle Suche nach Sprache
CREATE INDEX idx_user_legacies_user_id ON user_legacies(user_id);   -- Schnelle Suche nach Benutzer
CREATE INDEX idx_user_legacies_share_token ON user_legacies(share_token);  -- Schnelle Suche nach Share-Token

-- ============================================
-- Migration 5: Trigger fuer automatische updated_at-Aktualisierung
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
-- Migration 6: Tabellen-Kommentare fuer Dokumentation in Supabase
-- ============================================
COMMENT ON TABLE user_agents IS 'Stores PROMISE agent IDs for each user and language';
COMMENT ON TABLE user_legacies IS 'Stores completed legacy stories from the Biographer';
COMMENT ON COLUMN user_legacies.legacy_data IS 'JSONB containing all answers and summary from PROMISE';
COMMENT ON COLUMN user_legacies.share_token IS 'UUID token for sharing legacy with others';
