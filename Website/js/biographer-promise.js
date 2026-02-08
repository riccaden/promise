// ============================================
// PROMISE API INTEGRATION FOR BIOGRAPHER
// ============================================

// Use configuration from config.js
const PROMISE_CONFIG = {
    url: window.OBLIVIO_CONFIG?.PROMISE_API_URL || 'http://localhost:8080',
    agentType: window.OBLIVIO_CONFIG?.AGENT_TYPES.BIOGRAPHER || 2,
};

// ============================================
// PROMISE API FUNCTIONS
// ============================================

/**
 * Get or create a PROMISE agent for the current user
 */
async function getOrCreatePromiseAgent(userId, language = 'en') {
    // Check if user already has an agent ID stored in Supabase
    const { data: existingAgent, error: fetchError } = await supabaseClient
        .from('user_agents')
        .select('agent_id, is_active')
        .eq('user_id', userId)
        .eq('language', language)
        .single();

    if (existingAgent && !fetchError) {
        console.log('Found existing agent:', existingAgent.agent_id);

        // Check if agent still exists in PROMISE
        try {
            const response = await fetch(`${PROMISE_CONFIG.url}/${existingAgent.agent_id}/info`);
            if (response.ok) {
                return existingAgent.agent_id;
            }
        } catch (error) {
            console.log('Existing agent not found in PROMISE, creating new one...');
        }
    }

    // Create new agent
    console.log('Creating new PROMISE agent...');
    const agentName = language === 'de' ? 'Biograf' : 'Biographer';
    const agentDescription = language === 'de'
        ? 'Führt Benutzer durch die Erstellung ihres digitalen Erbes'
        : 'Guides users through creating their digital legacy';

    const response = await fetch(`${PROMISE_CONFIG.url}/agent/biographer`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            type: PROMISE_CONFIG.agentType,
            userId: userId,
            agentName: agentName,
            agentDescription: agentDescription,
            language: language
        })
    });

    if (!response.ok) {
        throw new Error('Failed to create PROMISE agent');
    }

    const data = await response.json();
    const agentId = data.id;

    // Store agent ID in Supabase
    await supabaseClient
        .from('user_agents')
        .upsert({
            user_id: userId,
            agent_id: agentId,
            language: language,
            is_active: true,
            created_at: new Date().toISOString()
        });

    console.log('Created new agent:', agentId);
    return agentId;
}

/**
 * Start a conversation with the PROMISE agent
 */
async function startPromiseConversation(agentId) {
    const response = await fetch(`${PROMISE_CONFIG.url}/${agentId}/start`, {
        method: 'POST'
    });

    if (!response.ok) {
        throw new Error('Failed to start conversation');
    }

    const data = await response.json();
    return {
        text: data.assistantResponse.text,
        stateName: data.assistantResponse.stateName,
        active: data.active
    };
}

/**
 * Send a user response to PROMISE and get the next question
 */
async function sendToPromise(agentId, userMessage) {
    const response = await fetch(`${PROMISE_CONFIG.url}/${agentId}/respond`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content: userMessage })
    });

    if (!response.ok) {
        throw new Error('Failed to send message to PROMISE');
    }

    const data = await response.json();
    return {
        text: data.assistantResponse.text,
        stateName: data.assistantResponse.stateName,
        active: data.active // false when conversation is complete
    };
}

/**
 * Get the conversation history from PROMISE
 */
async function getPromiseConversation(agentId) {
    const response = await fetch(`${PROMISE_CONFIG.url}/${agentId}/conversation`);

    if (!response.ok) {
        throw new Error('Failed to fetch conversation');
    }

    return await response.json();
}

/**
 * Get the stored legacy data from PROMISE
 */
async function getPromiseLegacyData(agentId) {
    const response = await fetch(`${PROMISE_CONFIG.url}/${agentId}/storage`);

    if (!response.ok) {
        throw new Error('Failed to fetch legacy data');
    }

    const storageData = await response.json();

    // Convert storage array to object
    const legacyObject = {};
    storageData.forEach(entry => {
        legacyObject[entry.key] = JSON.parse(entry.value);
    });

    return legacyObject;
}

/**
 * Save completed legacy to Supabase
 */
async function saveLegacyToSupabase(userId, agentId, legacyData) {
    const { error } = await supabaseClient
        .from('user_legacies')
        .insert({
            user_id: userId,
            agent_id: agentId,
            legacy_data: legacyData,
            created_at: new Date().toISOString(),
            completed_at: new Date().toISOString()
        });

    if (error) {
        console.error('Error saving legacy to Supabase:', error);
        throw error;
    }

    console.log('Legacy saved to Supabase successfully!');
}

/**
 * Reset/Delete agent conversation
 */
async function resetPromiseConversation(agentId) {
    const response = await fetch(`${PROMISE_CONFIG.url}/${agentId}/reset`, {
        method: 'DELETE'
    });

    if (!response.ok) {
        throw new Error('Failed to reset conversation');
    }

    return await response.json();
}

// ============================================
// SUPABASE CHAT PERSISTENCE FUNCTIONS
// ============================================

/**
 * Save a chat message to Supabase
 */
async function saveChatMessage(userId, agentId, role, content) {
    try {
        const { error } = await supabaseClient
            .from('chat_messages')
            .insert({
                user_id: userId,
                agent_id: agentId,
                role: role,
                content: content,
                created_at: new Date().toISOString()
            });

        if (error) {
            console.error('Error saving message to Supabase:', error);
            throw error;
        }

        console.log(`Message saved to Supabase (${role})`);
    } catch (error) {
        console.error('Failed to save message:', error);
        // Don't throw - we don't want to break the chat if saving fails
    }
}

/**
 * Load chat messages from Supabase
 */
async function loadChatMessages(userId, agentId) {
    try {
        const { data, error } = await supabaseClient
            .from('chat_messages')
            .select('role, content, created_at')
            .eq('user_id', userId)
            .eq('agent_id', agentId)
            .order('created_at', { ascending: true });

        if (error) {
            console.error('Error loading messages from Supabase:', error);
            return [];
        }

        console.log(`Loaded ${data?.length || 0} messages from Supabase`);
        return data || [];
    } catch (error) {
        console.error('Failed to load messages:', error);
        return [];
    }
}

/**
 * Delete all chat messages for a specific agent
 */
async function deleteChatMessages(userId, agentId) {
    try {
        const { error } = await supabaseClient
            .from('chat_messages')
            .delete()
            .eq('user_id', userId)
            .eq('agent_id', agentId);

        if (error) {
            console.error('Error deleting messages from Supabase:', error);
            throw error;
        }

        console.log('Messages deleted from Supabase');
    } catch (error) {
        console.error('Failed to delete messages:', error);
    }
}

// ============================================
// EXPORT FOR USE IN BIOGRAPHER.HTML
// ============================================
window.PromiseAPI = {
    getOrCreateAgent: getOrCreatePromiseAgent,
    startConversation: startPromiseConversation,
    sendMessage: sendToPromise,
    getConversation: getPromiseConversation,
    getLegacyData: getPromiseLegacyData,
    saveLegacy: saveLegacyToSupabase,
    resetConversation: resetPromiseConversation,
    // Supabase persistence functions
    saveMessage: saveChatMessage,
    loadMessages: loadChatMessages,
    deleteMessages: deleteChatMessages
};

console.log('✅ PROMISE API integration loaded');
