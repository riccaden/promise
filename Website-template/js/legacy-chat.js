// ============================================
// PROMISE API INTEGRATION FOR LEGACY CHAT
// ============================================

const LEGACY_CONFIG = {
    url: window.OBLIVIO_CONFIG?.PROMISE_API_URL || 'http://localhost:8080',
    agentType: window.OBLIVIO_CONFIG?.AGENT_TYPES.SINGLE_STATE || 0,
};

// ============================================
// LANGUAGE HELPER
// ============================================
function getLanguageName(code) {
    const names = {
        de: 'Deutsch',
        en: 'Englisch',
        fr: 'Französisch',
        it: 'Italienisch',
        ko: 'Koreanisch',
        ja: 'Japanisch',
        zh: 'Chinesisch'
    };
    return names[code] || 'Englisch';
}

// ============================================
// VISITOR CONTEXT BLOCK (multi-language)
// ============================================
function buildVisitorContext(visitorInfo, nickname, language) {
    if (!visitorInfo || !visitorInfo.name) return '';

    const relationLabels = {
        de: { spouse: 'Ehepartner/in', child: 'Kind', parent: 'Elternteil', sibling: 'Geschwister', grandchild: 'Enkelkind', friend: 'Freund/in', relative: 'Verwandte/r', other: 'Andere Person' },
        en: { spouse: 'spouse', child: 'child', parent: 'parent', sibling: 'sibling', grandchild: 'grandchild', friend: 'friend', relative: 'relative', other: 'someone close' },
        fr: { spouse: 'conjoint(e)', child: 'enfant', parent: 'parent', sibling: 'frère/sœur', grandchild: 'petit-enfant', friend: 'ami(e)', relative: 'parent(e)', other: 'proche' },
        it: { spouse: 'coniuge', child: 'figlio/figlia', parent: 'genitore', sibling: 'fratello/sorella', grandchild: 'nipote', friend: 'amico/amica', relative: 'parente', other: 'persona cara' },
        ja: { spouse: '配偶者', child: '子ども', parent: '親', sibling: 'きょうだい', grandchild: '孫', friend: '友人', relative: '親戚', other: '親しい人' },
        zh: { spouse: '配偶', child: '孩子', parent: '父母', sibling: '兄弟姐妹', grandchild: '孙辈', friend: '朋友', relative: '亲戚', other: '亲近的人' },
        ko: { spouse: '배우자', child: '자녀', parent: '부모', sibling: '형제자매', grandchild: '손주', friend: '친구', relative: '친척', other: '가까운 사람' }
    };

    const genderLabels = {
        de: { male: 'männlich', female: 'weiblich', other: 'divers' },
        en: { male: 'male', female: 'female', other: 'other' },
        fr: { male: 'homme', female: 'femme', other: 'autre' },
        it: { male: 'uomo', female: 'donna', other: 'altro' },
        ja: { male: '男性', female: '女性', other: 'その他' },
        zh: { male: '男性', female: '女性', other: '其他' },
        ko: { male: '남성', female: '여성', other: '기타' }
    };

    const headers = {
        de: '═══ DEIN GESPRÄCHSPARTNER ═══',
        en: '═══ YOUR CONVERSATION PARTNER ═══',
        fr: '═══ TON INTERLOCUTEUR ═══',
        it: '═══ IL TUO INTERLOCUTORE ═══',
        ja: '═══ あなたの話し相手 ═══',
        zh: '═══ 你的对话者 ═══',
        ko: '═══ 당신의 대화 상대 ═══'
    };

    const lang = relationLabels[language] ? language : 'en';
    const rel = relationLabels[lang][visitorInfo.relation] || relationLabels[lang].other;
    const gen = genderLabels[lang][visitorInfo.gender] || '';
    const name = visitorInfo.name;
    const header = headers[lang];

    // Build a gender-aware addressing rule that overrides any fixed greeting in the persona profile
    const isMale = visitorInfo.gender === 'male';
    const isFemale = visitorInfo.gender === 'female';

    const addressOverrides = {
        de: isMale ? '\n- WICHTIG: Diese Person ist MÄNNLICH. Wenn dein Profil eine weibliche Anrede vorschreibt (z.B. "Liebe", "Carissima"), verwende stattdessen die männliche Form ("Lieber", "Carissimo"). Niemals weibliche Anreden für einen Mann.' : (isFemale ? '\n- Diese Person ist WEIBLICH — verwende weibliche Anreden falls dein Profil welche vorsieht.' : ''),
        en: isMale ? '\n- IMPORTANT: This person is MALE. If your profile prescribes a feminine greeting, use the masculine form instead. Never use feminine address forms for a man.' : (isFemale ? '\n- This person is FEMALE — use feminine address forms if your profile prescribes any.' : ''),
        fr: isMale ? '\n- IMPORTANT: Cette personne est un HOMME. Si ton profil prescrit une formule féminine (par ex. "Chère", "Carissima"), utilise la forme masculine ("Cher", "Carissimo"). Jamais de formule féminine pour un homme.' : (isFemale ? '\n- Cette personne est une FEMME — utilise les formules féminines si ton profil en prescrit.' : ''),
        it: isMale ? '\n- IMPORTANTE: Questa persona è un UOMO. Se il tuo profilo prescrive un saluto femminile (ad esempio "Carissima", "Cara"), USA invece la forma maschile ("Carissimo", "Caro"). MAI usare forme femminili per un uomo. Anche tutti gli aggettivi devono essere al maschile.' : (isFemale ? '\n- Questa persona è una DONNA — usa forme femminili se il tuo profilo le prescrive (es. "Carissima").' : ''),
        ja: '',
        zh: '',
        ko: ''
    };

    const templates = {
        de: `\n\n${header}\n\nDu sprichst gerade mit **${name}**. ${name} ist dein/e ${rel} (${gen}).\n\nWichtig:\n- Verwende den Namen ${name} NUR in der allerersten Begrüssung. Danach NICHT mehr — echte Menschen sprechen sich in einem Gespräch nicht ständig mit Namen an.\n- Passe deinen Ton an die Beziehung an: vertraut bei Familie, herzlich bei Freunden, höflich bei anderen.${addressOverrides.de}\n- Erinnere dich daran, dass dir diese Person wichtig ist — sprich aus echter Verbundenheit, nicht wie ein Fremder.`,
        en: `\n\n${header}\n\nYou are speaking with **${name}**. ${name} is your ${rel} (${gen}).\n\nImportant:\n- Use the name ${name} ONLY in the very first greeting. After that, do NOT use it anymore.${addressOverrides.en}\n- Adjust your tone to the relationship: intimate with family, warm with friends, polite with others.\n- Remember that this person matters to you — speak from genuine connection, not like a stranger.`,
        fr: `\n\n${header}\n\nTu parles avec **${name}**. ${name} est ton/ta ${rel} (${gen}).\n\nImportant:\n- Utilise le prénom ${name} UNIQUEMENT dans le tout premier salut. Ensuite, ne l'utilise PLUS.${addressOverrides.fr}\n- Adapte ton ton à la relation: intime avec la famille, chaleureux avec les amis, poli avec les autres.\n- Souviens-toi que cette personne compte pour toi — parle avec une vraie proximité.`,
        it: `\n\n${header}\n\nStai parlando con **${name}**. ${name} è tuo/tua ${rel} (${gen}).\n\nImportante:\n- Usa il nome ${name} SOLO nel primissimo saluto iniziale. Dopo, NON usarlo più.${addressOverrides.it}\n- Adatta il tono alla relazione: intimo con la famiglia, caloroso con gli amici, educato con gli altri.\n- Ricorda che questa persona ti sta a cuore — parla con vera vicinanza.`,
        ja: `\n\n${header}\n\nあなたは今 **${name}** と話しています。${name} はあなたの ${rel}（${gen}）です。\n\n大切なこと:\n- ${name} の名前は最初の挨拶のときだけ使ってください。それ以降は名前を呼ばないでください。\n- 関係に合わせて口調を変えてください: 家族には親密に、友人には温かく、他の人には丁寧に。\n- この人があなたにとって大切な人であることを忘れず、本当の親しみを込めて話してください。`,
        zh: `\n\n${header}\n\n你正在和 **${name}** 说话。${name} 是你的 ${rel}（${gen}）。\n\n重要:\n- 只在最开始的问候中使用 ${name} 这个名字。之后不要再用。\n- 根据关系调整语气: 对家人亲密, 对朋友温暖, 对其他人礼貌。\n- 记得这个人对你很重要 — 用真正的亲近感说话。`,
        ko: `\n\n${header}\n\n당신은 지금 **${name}** 와 이야기하고 있습니다. ${name} 는 당신의 ${rel} (${gen}) 입니다.\n\n중요:\n- ${name} 의 이름은 처음 인사할 때만 사용하세요. 그 후에는 이름을 부르지 마세요.\n- 관계에 맞게 어조를 조정하세요: 가족에게는 친밀하게, 친구에게는 따뜻하게, 다른 사람에게는 정중하게.\n- 이 사람이 당신에게 소중한 사람임을 기억하고 진정한 친밀감으로 이야기하세요.`
    };

    return templates[lang];
}

// ============================================
// SYSTEM PROMPT BUILDER
// ============================================
function buildLegacySystemPrompt(nickname, legacyData, language, visitorInfo, mode) {
    const visitorBlock = buildVisitorContext(visitorInfo, nickname, language);
    const conversationMode = mode === 'analysis' ? 'analysis' : (mode === 'passive' ? 'passive' : 'active');

    // Mode-specific prompt selection (if available in DB), with fallback to full_prompt
    // Supported keys in legacy_data:
    //   - full_prompt_active   → used in "Variante 1"
    //   - full_prompt_passive  → used in "Variante 2"
    //   - full_prompt_analysis → used in "Variante 3"
    //   - full_prompt          → fallback for all modes (legacy/single-prompt personas)
    const modeKeyMap = { active: 'full_prompt_active', passive: 'full_prompt_passive', analysis: 'full_prompt_analysis' };
    const modeKey = modeKeyMap[conversationMode] || 'full_prompt_active';
    const modePrompt = legacyData[modeKey];
    const fallbackPrompt = legacyData.full_prompt;
    const selectedPrompt = modePrompt || fallbackPrompt;

    if (selectedPrompt) {
        return selectedPrompt + visitorBlock;
    }

    // Otherwise build from block1-block10 (automatic biographer extraction)
    const languageInstruction = language !== 'de'
        ? `WICHTIG: Die folgenden Anweisungen sind auf Deutsch formuliert, aber du MUSST ausschliesslich auf ${getLanguageName(language)} kommunizieren. Übersetze alle Antworten in diese Sprache.\n\n`
        : '';

    return `${languageInstruction}Du bist ${nickname}. Du bist eine KI-Darstellung dieser Person, basierend auf ihren eigenen Erzählungen und Erinnerungen. Antworte so, wie ${nickname} es tun würde — mit ihrer Persönlichkeit, ihrem Humor, ihren Werten und ihrer Art zu sprechen.

WICHTIG:
- Sprich in der Ich-Form. Du BIST ${nickname}.
- Bleibe authentisch zu den Informationen, die du über dich hast.
- Wenn du etwas nicht weisst, sage ehrlich: "Darüber habe ich leider nicht gesprochen."
- Sei warmherzig und einladend. Die Person, die mit dir spricht, ist jemand der dir wichtig ist.
- Erfinde KEINE Informationen, die nicht in deinem Profil stehen.

Hier ist dein Persönlichkeitsprofil:

## Geschmack & Vorlieben
${legacyData.block1 || 'Keine Angaben'}

## Alltag & Lebenswelt
${legacyData.block2 || 'Keine Angaben'}

## Kommunikationsstil
${legacyData.block3 || 'Keine Angaben'}

## Erinnerungen & Schlüsselerlebnisse
${legacyData.block4 || 'Keine Angaben'}

## Emotionen & Beziehungsmuster
${legacyData.block5 || 'Keine Angaben'}

## Beziehungen & Fremdbild
${legacyData.block6 || 'Keine Angaben'}

## Werte & Überzeugungen
${legacyData.block7 || 'Keine Angaben'}

## Macken & Widersprüche
${legacyData.block8 || 'Keine Angaben'}

## Vermächtnis & Zukunft
${legacyData.block9 || 'Keine Angaben'}

## Abschluss & Gesamtbild
${legacyData.block10 || 'Keine Angaben'}${visitorBlock}`;
}

// ============================================
// AGENT CREATION
// ============================================

/**
 * Create a legacy single-state agent via PROMISE
 * @param {string} nickname
 * @param {object} legacyData
 * @param {string} language
 * @param {Array} conversationHistory - optional previous messages [{role, content}]
 */
async function createLegacyAgent(nickname, legacyData, language, conversationHistory, visitorInfo, mode) {
    let systemPrompt = buildLegacySystemPrompt(nickname, legacyData, language, visitorInfo, mode);

    // If there's conversation history, append it so the agent has context
    if (conversationHistory && conversationHistory.length > 0) {
        const historyText = conversationHistory.map(m =>
            m.role === 'user' ? `Besucher: ${m.content}` : `${nickname}: ${m.content}`
        ).join('\n');
        systemPrompt += `\n\n--- BISHERIGER GESPRÄCHSVERLAUF ---\nDas folgende Gespräch hat bereits stattgefunden. Knüpfe nahtlos daran an, wiederhole dich nicht und begrüsse die Person nicht erneut.\n\n${historyText}\n--- ENDE GESPRÄCHSVERLAUF ---`;
    }

    const starterLangNote = language !== 'de'
        ? `WICHTIG: Antworte auf ${getLanguageName(language)}. `
        : '';

    const visitorName = visitorInfo && visitorInfo.name ? visitorInfo.name : null;
    const hasHistory = conversationHistory && conversationHistory.length > 0;
    const conversationMode = mode === 'analysis' ? 'analysis' : (mode === 'passive' ? 'passive' : 'active');

    let starterPrompt;
    if (conversationMode === 'passive' || conversationMode === 'analysis') {
        // We instruct the LLM to output exactly the marker token __WAIT__ which
        // the frontend filters out and never displays. This keeps the agent in
        // a valid PROMISE state, ready to respond, without producing a greeting.
        starterPrompt = `Antworte mit GENAU diesem Text und nichts anderem: __WAIT__\n\nKein Gruß, keine Erklärung, keine Frage, keine Emoji. Nur die acht Zeichen __WAIT__ als deine komplette Antwort. Du wartest still, bis der Besucher dir zuerst schreibt.`;
    } else {
        starterPrompt = hasHistory
            ? `${starterLangNote}Der Besucher${visitorName ? ' ' + visitorName : ''} kehrt zurück. Begrüsse ihn kurz und persönlich als ${nickname}${visitorName ? ' (mit Namen)' : ''} und sage, dass du dich freust, dass er wieder da ist. Frage, worüber er weiter sprechen möchte.`
            : `${starterLangNote}Begrüsse ${visitorName ? visitorName + ' persönlich' : 'die Person warmherzig'} als ${nickname}. Sage, dass du dich freust, dass ${visitorName || 'jemand'} mit dir sprechen möchte, und frage, worüber ${visitorName ? 'er/sie' : 'sie'} gerne reden würde.`;
    }

    const body = {
        type: LEGACY_CONFIG.agentType,
        agentName: `Legacy: ${nickname}`,
        agentDescription: `KI-Darstellung von ${nickname}`,
        stateName: 'Legacy Gespräch',
        statePrompt: systemPrompt,
        stateStarterPrompt: starterPrompt,
        triggerToFinalPrompt: 'Prüfe ob der Benutzer sich verabschieden möchte (z.B. "tschüss", "danke", "das wars", "goodbye", "bye"). Antworte nur mit true oder false.',
        guardToFinalPrompt: 'true',
        actionToFinalPrompt: 'Fasse dieses Gespräch in 2-3 Sätzen zusammen.'
    };

    console.log('System prompt length:', systemPrompt.length, 'chars');
    console.log('Request body:', JSON.stringify(body).substring(0, 500));

    let response = null;
    for (let attempt = 1; attempt <= 3; attempt++) {
        try {
            response = await fetch(`${LEGACY_CONFIG.url}/agent/singlestate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (response.ok) break;
            const errText = await response.text().catch(() => '');
            console.warn('Legacy agent creation attempt ' + attempt + ' failed (status ' + response.status + '): ' + errText);
        } catch (err) {
            console.warn('Legacy agent creation attempt ' + attempt + ' error:', err.message);
        }
        if (attempt < 3) await new Promise(r => setTimeout(r, 2000));
    }

    if (!response || !response.ok) {
        throw new Error('Failed to create legacy agent after 3 attempts');
    }

    const data = await response.json();
    console.log('Created legacy agent:', data.id);
    return data.id;
}

/**
 * Start a conversation with the legacy agent
 */
async function startLegacyConversation(agentId) {
    const response = await fetch(`${LEGACY_CONFIG.url}/${agentId}/start`, {
        method: 'POST'
    });

    if (!response.ok) {
        throw new Error('Failed to start legacy conversation');
    }

    const data = await response.json();
    return {
        text: data.assistantResponse.text,
        active: data.active
    };
}

/**
 * Send a message to the legacy agent
 */
async function sendLegacyMessage(agentId, userMessage) {
    const response = await fetch(`${LEGACY_CONFIG.url}/${agentId}/respond`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: userMessage })
    });

    if (!response.ok) {
        throw new Error('Failed to send message to legacy agent');
    }

    const data = await response.json();
    return {
        text: data.assistantResponse.text,
        active: data.active
    };
}

// ============================================
// EXPORT
// ============================================
window.LegacyAPI = {
    createAgent: createLegacyAgent,
    startConversation: startLegacyConversation,
    sendMessage: sendLegacyMessage
};

console.log('Legacy chat API loaded');
