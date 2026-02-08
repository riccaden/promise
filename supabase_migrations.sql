-- ============================================
-- SUPABASE MIGRATIONS FOR PROMISE INTEGRATION
-- ============================================
-- Run these in your Supabase SQL Editor

-- Table to store user's PROMISE agent IDs
CREATE TABLE IF NOT EXISTS user_agents (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, language)
);

-- Table to store completed legacies
CREATE TABLE IF NOT EXISTS user_legacies (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    agent_id UUID NOT NULL,
    legacy_data JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    is_shared BOOLEAN DEFAULT false,
    share_token UUID DEFAULT gen_random_uuid()
);

-- Enable Row Level Security
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

-- Indexes for better performance
CREATE INDEX idx_user_agents_user_id ON user_agents(user_id);
CREATE INDEX idx_user_agents_language ON user_agents(language);
CREATE INDEX idx_user_legacies_user_id ON user_legacies(user_id);
CREATE INDEX idx_user_legacies_share_token ON user_legacies(share_token);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to auto-update updated_at
CREATE TRIGGER update_user_agents_updated_at
    BEFORE UPDATE ON user_agents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE user_agents IS 'Stores PROMISE agent IDs for each user and language';
COMMENT ON TABLE user_legacies IS 'Stores completed legacy stories from the Biographer';
COMMENT ON COLUMN user_legacies.legacy_data IS 'JSONB containing all answers and summary from PROMISE';
COMMENT ON COLUMN user_legacies.share_token IS 'UUID token for sharing legacy with others';
