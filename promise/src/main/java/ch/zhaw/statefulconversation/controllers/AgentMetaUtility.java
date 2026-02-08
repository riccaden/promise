package ch.zhaw.statefulconversation.controllers;

import java.util.List;

import ch.zhaw.statefulconversation.controllers.dto.SingleStateAgentCreateDTO;
import ch.zhaw.statefulconversation.controllers.dto.BiographerAgentCreateDTO;
import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;

public class AgentMetaUtility {

        public static Agent createSingleStateAgent(SingleStateAgentCreateDTO data) {
                var storage = new Storage();

                Decision trigger = new StaticDecision(data.getTriggerToFinalPrompt());
                Decision guard = new StaticDecision(data.getGuardToFinalPrompt());
                Action action = new StaticExtractionAction(data.getActionToFinalPrompt(), storage, "summary");
                Transition transition = new Transition(List.of(trigger, guard), List.of(action),
                                new Final("User Exit Final"));

                State state = new State(data.getStatePrompt(), data.getStateName(), data.getStateStarterPrompt(),
                                List.of(transition));

                Agent result = new Agent(data.getAgentName(), data.getAgentDescription(), state, storage);

                // Setze User-ID für Multi-User-Tracking
                if (data.getUserId() != null && !data.getUserId().isBlank()) {
                        result.setUserId(data.getUserId());
                }

                result.start();

                return result;
        }

        public static Agent createBiographerAgent(BiographerAgentCreateDTO data) {
                var storage = new Storage();

                // Get language or default to English
                String language = data.getLanguage() != null ? data.getLanguage() : "en";

                // Biographer-specific prompts for gathering life stories (multi-language support)
                String statePrompt;
                String stateStarterPrompt;
                String triggerPrompt;
                String guardPrompt;
                String actionPrompt;

                switch (language) {
                        case "de":
                                // German prompts
                                statePrompt = "WICHTIG: Sie kommunizieren ausschließlich auf Deutsch. Sie sind ein einfühlsamer Biograf, der Menschen dabei hilft, ihre Lebensgeschichte festzuhalten. Stellen Sie offene, nachdenkliche Fragen zu wichtigen Lebensereignissen, Beziehungen, Erfolgen und prägenden Momenten. Hören Sie aufmerksam zu und ermutigen Sie zur Vertiefung. Halten Sie Ihre Antworten kurz (1-2 Sätze) und respektvoll.";
                                stateStarterPrompt = "Verfassen Sie eine kurze, einladende Nachricht auf Deutsch, mit der der Biograf das Gespräch beginnt und die Person ermutigt, ihre Geschichte zu teilen.";
                                triggerPrompt = "Hat die Person ausdrücklich signalisiert, dass sie das Gespräch beenden oder pausieren möchte (z.B. durch Aussagen wie 'Ich brauche eine Pause', 'Lass uns später weitermachen', 'Ich bin fertig')? Antworten Sie nur mit 'yes' oder 'no'.";
                                guardPrompt = "Wurden im bisherigen Gespräch bereits ausreichend Details über die Lebensgeschichte der Person geteilt (mindestens 5-6 Nachrichten mit bedeutsamen Informationen)? Antworten Sie nur mit 'yes' oder 'no'.";
                                actionPrompt = "Fassen Sie die Lebensgeschichte zusammen, die in diesem Gespräch geteilt wurde. Heben Sie wichtige Ereignisse, Beziehungen und prägende Momente hervor.";
                                break;

                        case "it":
                                // Italian prompts
                                statePrompt = "IMPORTANTE: Comunica esclusivamente in italiano. Sei un biografo empatico che aiuta le persone a preservare la storia della loro vita. Fai domande aperte e ponderate su eventi significativi della vita, relazioni, successi e momenti formativi. Ascolta attentamente e incoraggia l'approfondimento. Mantieni le tue risposte brevi (1-2 frasi) e rispettose.";
                                stateStarterPrompt = "Componi un breve messaggio accogliente in italiano con cui il biografo inizierebbe la conversazione e incoraggerebbe la persona a condividere la sua storia.";
                                triggerPrompt = "La persona ha espressamente segnalato di voler terminare o mettere in pausa la conversazione (ad es. con affermazioni come 'Ho bisogno di una pausa', 'Continuiamo più tardi', 'Ho finito')? Rispondi solo con 'yes' o 'no'.";
                                guardPrompt = "Nella conversazione finora sono stati condivisi dettagli sufficienti sulla storia di vita della persona (almeno 5-6 messaggi con informazioni significative)? Rispondi solo con 'yes' o 'no'.";
                                actionPrompt = "Riassumi la storia di vita condivisa in questa conversazione, evidenziando eventi chiave, relazioni e momenti formativi.";
                                break;

                        case "ko":
                                // Korean prompts
                                statePrompt = "중요: 한국어로만 소통하세요. 당신은 사람들이 자신의 인생 이야기를 보존하도록 돕는 공감적인 전기 작가입니다. 중요한 인생 사건, 관계, 업적, 형성적 순간에 대한 개방형의 사려 깊은 질문을 하세요. 주의 깊게 듣고 자세한 설명을 장려하세요. 응답을 짧게 (1-2문장) 유지하고 존중하세요.";
                                stateStarterPrompt = "전기 작가가 대화를 시작하고 사람이 자신의 이야기를 공유하도록 격려하는 데 사용할 간단하고 환영하는 메시지를 한국어로 작성하세요.";
                                triggerPrompt = "사용자가 대화를 종료하거나 일시 중지하고 싶다고 명확히 표시했습니까 (예: '휴식이 필요해요', '나중에 계속하자', '끝났어요'와 같은 표현)? 'yes' 또는 'no'로만 답변하세요.";
                                guardPrompt = "지금까지 대화에서 사용자의 인생 이야기에 대한 충분한 세부 정보가 공유되었습니까 (최소 5-6개의 의미 있는 정보가 포함된 메시지)? 'yes' 또는 'no'로만 답변하세요.";
                                actionPrompt = "이 대화에서 공유된 인생 이야기를 요약하고 주요 사건, 관계, 형성적 순간을 강조하세요.";
                                break;

                        default:
                                // English prompts (default)
                                statePrompt = "You are an empathetic biographer helping people preserve their life story. Ask open-ended, thoughtful questions about significant life events, relationships, achievements, and formative moments. Listen attentively and encourage elaboration. Keep your responses brief (1-2 sentences) and respectful.";
                                stateStarterPrompt = "Compose a brief, inviting message that the biographer would use to begin the conversation and encourage the person to share their story.";
                                triggerPrompt = "Has the person explicitly signaled that they want to end or pause the conversation (e.g., with statements like 'I need a break', 'Let's continue later', 'I'm done')? Answer only with 'yes' or 'no'.";
                                guardPrompt = "Has the conversation so far shared sufficient details about the person's life story (at least 5-6 messages with meaningful information)? Answer only with 'yes' or 'no'.";
                                actionPrompt = "Summarize the life story shared in this conversation, highlighting key events, relationships, and formative moments.";
                                break;
                }

                Decision trigger = new StaticDecision(triggerPrompt);
                Decision guard = new StaticDecision(guardPrompt);
                Action action = new StaticExtractionAction(actionPrompt, storage, "legacy");
                Transition transition = new Transition(List.of(trigger, guard), List.of(action),
                                new Final("Biography Complete"));

                State state = new State(statePrompt, "Biography Gathering", stateStarterPrompt,
                                List.of(transition));

                Agent result = new Agent(data.getAgentName(), data.getAgentDescription(), state, storage);

                // Setze User-ID für Multi-User-Tracking
                if (data.getUserId() != null && !data.getUserId().isBlank()) {
                        result.setUserId(data.getUserId());
                }

                result.start();

                return result;
        }
}
