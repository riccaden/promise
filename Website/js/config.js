// ============================================
// CONFIGURATION FOR OBLIVIO + PROMISE
// ============================================

const CONFIG = {
    // PROMISE Backend API URL
    // TODO: Replace with your actual Railway URL
    PROMISE_API_URL: 'https://promise-production.up.railway.app',

    // Supabase Configuration
    SUPABASE_URL: 'https://gmpgsozqcadbofezzloo.supabase.co',
    SUPABASE_ANON_KEY: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdtcGdzb3pxY2FkYm9mZXp6bG9vIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAzMzA2NjgsImV4cCI6MjA4NTkwNjY2OH0.uzGJNhUtklycGomEdbEraSbfXWCxPXW_shno7nNSdS8',

    // ElevenLabs TTS Configuration
    // API key is now securely stored on Railway Backend (not in frontend)
    // Voice is handled by PROMISE Backend

    // Agent Types
    AGENT_TYPES: {
        BIOGRAPHER: 2,
        SINGLE_STATE: 0
    }
};

// Export for use in other files
window.OBLIVIO_CONFIG = CONFIG;

console.log('✅ Oblivio configuration loaded');
