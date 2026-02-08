-- ============================================
-- SUPABASE TABLES FOR OBLIVIO + PROMISE
-- ============================================

-- Table: user_agents
-- Stores the mapping between users and their PROMISE agents
CREATE TABLE IF NOT EXISTS user_agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    language VARCHAR(5) DEFAULT 'en',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),

    -- Ensure one active agent per user per language
    UNIQUE(user_id, language, is_active)
);

-- Table: user_legacies
-- Stores completed legacy data from PROMISE conversations
CREATE TABLE IF NOT EXISTS user_legacies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    legacy_data JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    is_published BOOLEAN DEFAULT false,

    FOREIGN KEY (user_id, agent_id) REFERENCES user_agents(user_id, agent_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_agents_user_id ON user_agents(user_id);
CREATE INDEX IF NOT EXISTS idx_user_agents_agent_id ON user_agents(agent_id);
CREATE INDEX IF NOT EXISTS idx_user_legacies_user_id ON user_legacies(user_id);
CREATE INDEX IF NOT EXISTS idx_user_legacies_created_at ON user_legacies(created_at DESC);

-- Enable Row Level Security (RLS)
ALTER TABLE user_agents ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_legacies ENABLE ROW LEVEL SECURITY;

-- RLS Policies for user_agents
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

-- RLS Policies for user_legacies
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

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to automatically update updated_at
CREATE TRIGGER update_user_agents_updated_at
    BEFORE UPDATE ON user_agents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- DONE! 🎉
-- Run this SQL in Supabase SQL Editor
-- ============================================
