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
async function getOrCreatePromiseAgent(userId, language = 'en', nickname = null) {
    // Check if user already has an agent ID stored in Supabase
    const { data: existingAgent, error: fetchError } = await supabaseClient
        .from('user_agents')
        .select('agent_id, is_active, nickname')
        .eq('user_id', userId)
        .eq('language', language)
        .single();

    if (existingAgent && !fetchError) {
        // Only reuse agent if it was created with the same nickname
        if (existingAgent.nickname === nickname) {
            console.log('Found existing agent with matching nickname:', existingAgent.agent_id);
            try {
                const response = await fetch(`${PROMISE_CONFIG.url}/${existingAgent.agent_id}/info`);
                if (response.ok) {
                    return existingAgent.agent_id;
                }
            } catch (error) {
                console.log('Existing agent not found in PROMISE, creating new one...');
            }
        } else {
            console.log('Agent nickname mismatch (stored:', existingAgent.nickname, '/ current:', nickname, '), recreating with correct nickname');
        }
    }

    // Create new agent in PROMISE (with retry for cold-start)
    console.log('Creating new PROMISE agent...');
    const agentName = language === 'de' ? 'Biograf' : 'Biographer';
    const agentDescription = language === 'de'
        ? 'Führt Benutzer durch die Erstellung ihres digitalen Erbes'
        : 'Guides users through creating their digital legacy';

    let response = null;
    for (let attempt = 1; attempt <= 3; attempt++) {
        try {
            response = await fetch(`${PROMISE_CONFIG.url}/agent/biographer`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    type: PROMISE_CONFIG.agentType,
                    userId: userId,
                    agentName: agentName,
                    agentDescription: agentDescription,
                    language: language,
                    nickname: nickname
                })
            });
            if (response.ok) break;
            console.warn('Agent creation attempt ' + attempt + ' failed (status ' + response.status + ')');
        } catch (err) {
            console.warn('Agent creation attempt ' + attempt + ' error:', err.message);
        }
        if (attempt < 3) await new Promise(r => setTimeout(r, 2000));
    }

    if (!response || !response.ok) {
        throw new Error('Failed to create PROMISE agent after 3 attempts');
    }

    const data = await response.json();
    const agentId = data.id;

    // Store agent ID in Supabase (upsert by user_id + language)
    await supabaseClient
        .from('user_agents')
        .upsert({
            user_id: userId,
            agent_id: agentId,
            language: language,
            is_active: true,
            nickname: nickname,
            created_at: new Date().toISOString()
        }, { onConflict: 'user_id,language' });

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
 * Load ALL chat messages for a user regardless of agent_id.
 * Fallback for when PROMISE restarted and a new agent_id was created.
 */
async function loadAllUserChatMessages(userId) {
    try {
        const { data, error } = await supabaseClient
            .from('chat_messages')
            .select('role, content, created_at')
            .eq('user_id', userId)
            .order('created_at', { ascending: true });

        if (error) {
            console.error('Error loading all user messages from Supabase:', error);
            return [];
        }

        console.log(`Loaded ${data?.length || 0} total messages for user from Supabase`);
        return data || [];
    } catch (error) {
        console.error('Failed to load all user messages:', error);
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
// BIOGRAPHER CONVERSATIONS TABLE (with block/state tracking)
// ============================================

/**
 * Save a message to biographer_conversations with block number and state name
 */
async function saveBiographerConversation(userId, agentId, role, content, stateName) {
    try {
        // Extract block number from state name
        let blockNumber = null;
        if (stateName) {
            const baseName = stateName.replace(' - Bestätigung', '');
            const match = baseName.match(/Block (\d+)/);
            if (match) blockNumber = parseInt(match[1], 10);
        }

        const { error } = await supabaseClient
            .from('biographer_conversations')
            .insert({
                user_id: userId,
                agent_id: agentId,
                block_number: blockNumber,
                state_name: stateName || null,
                role: role,
                content: content,
                created_at: new Date().toISOString()
            });

        if (error) {
            console.error('Error saving to biographer_conversations:', error);
        }
    } catch (error) {
        console.error('Failed to save biographer conversation:', error);
        // Don't throw - saving should not break the chat
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
    loadAllUserMessages: loadAllUserChatMessages,
    deleteMessages: deleteChatMessages,
    // Biographer conversation logging
    saveBiographerConversation: saveBiographerConversation
};

console.log('✅ PROMISE API integration loaded');
