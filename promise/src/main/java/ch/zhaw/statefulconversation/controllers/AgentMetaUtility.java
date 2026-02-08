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

        /**
         * Creates a structured Biographer agent with consent, basic info collection,
         * and comprehensive questionnaire flow (multi-state interaction).
         */
        public static Agent createStructuredBiographerAgent(BiographerAgentCreateDTO data) {
                var storage = new Storage();

                // Get language or default to German
                String language = data.getLanguage() != null ? data.getLanguage() : "de";

                // Language-specific prompts for structured questionnaire
                String consentPrompt;
                String consentStarter;
                String questionsPrompt;
                String questionsStarter;
                String thankYouPrompt;
                String thankYouMessage;

                switch (language) {
                        case "de":
                                // German - Consent and Basic Info State
                                consentStarter = """
                                        Willkommen!

                                        Dieser Fragebogen dient dazu, deinen individuellen Schreibstil und deine Persönlichkeit zu erfassen.
                                        Deine Antworten werden verwendet, um ein KI-Modell zu trainieren, das deinen Kommunikationsstil nachbilden kann.

                                        📋 Wichtige Datenschutz-Hinweise:
                                        • Alle Daten werden ausschließlich für Forschungszwecke verwendet
                                        • Du kannst Pseudonyme oder Fake-Namen verwenden
                                        • Demografische Daten werden nur in groben Kategorien erfasst

                                        Bereit? Dann lass uns starten!

                                        Bist du damit einverstanden, dass deine Antworten für Forschungszwecke (Bachelorarbeit) verwendet werden? (Ja/Nein)
                                        """;

                                consentPrompt = """
                                        Du sammelst Einverständnis und Basisdaten für eine Forschungsstudie.

                                        Stelle folgende Fragen der Reihe nach, EINE pro Nachricht:
                                        1. Einverständnis zur Datennutzung (bereits gestellt)
                                        2. Wie möchtest du genannt werden?
                                        3. Altersgruppe? (unter 18 / 18-24 / 25-34 / 35-44 / 45-54 / 55-64 / 65+)
                                        4. Geschlecht? (Männlich / Weiblich / Nicht-binär / Möchte ich nicht angeben)
                                        5. Welche Messenger nutzt du hauptsächlich? (WhatsApp, Telegram, Signal, etc.)
                                        6. Bevorzugst du viele kurze oder eine lange Nachricht?
                                        7. Wie würdest du dich selbst beschreiben? (Introvertiert/Extrovertiert/Analytisch/Emotional/etc.)
                                        8. Dein Kommunikationsstil ist eher...? (Direkt/Ausführlich/Emotional/Sachlich/Humorvoll/etc.)

                                        Sei freundlich, kurz und klar. Bestätige jede Antwort kurz und stelle dann die nächste Frage.
                                        """;

                                // German - Deep Questions State
                                questionsStarter = "Perfekt! Jetzt kommen die ausführlicheren Fragen. Nimm dir Zeit für deine Antworten. Fangen wir an:\n\nErzähl mir von deinem gestrigen Tag. Was hast du gemacht?";

                                questionsPrompt = """
                                        Du bist ein einfühlsamer Interviewer, der tiefgründige Fragen zum Schreibstil und zur Persönlichkeit stellt.

                                        Stelle EINE Frage pro Nachricht aus folgenden Bereichen (in dieser Reihenfolge):

                                        BLOCK 1 - ALLTÄGLICHE KOMMUNIKATION:
                                        1. Erzähl mir von deinem gestrigen Tag. Was hast du gemacht?
                                        2. Wie würdest du einem Freund per Nachricht erklären, wie man Kaffee kocht?
                                        3. Beschreibe dein Lieblingsessen so, dass mir das Wasser im Mund zusammenläuft.
                                        4. Was machst du, wenn du nicht schlafen kannst? Erzähl mal.
                                        5. Stell dir vor, du gewinnst morgen im Lotto. Schreib mir spontan deine erste Reaktion.

                                        BLOCK 2 - EMOTIONALE AUSDRUCKSWEISE (Freude & Begeisterung):
                                        6. Erzähl mir von einem Moment, in dem du richtig glücklich warst. Was ist passiert?
                                        7. Jemand schenkt dir genau das, was du dir schon lange gewünscht hast. Wie reagierst du?
                                        8. Was war das Lustigste, das dir in letzter Zeit passiert ist?

                                        BLOCK 2 - EMOTIONALE AUSDRUCKSWEISE (Frustration & Ärger):
                                        9. Beschreibe eine Situation, die dich so richtig nervt oder genervt hat.
                                        10. Du wartest seit 30 Minuten auf jemanden, der zu spät kommt. Was schreibst du der Person?
                                        11. Erzähl von etwas, das total schief gelaufen ist – wie hast du reagiert?

                                        BLOCK 2 - EMOTIONALE AUSDRUCKSWEISE (Empathie & Mitgefühl):
                                        12. Ein guter Freund schreibt dir: "Ich hatte einen furchtbaren Tag, alles ist schief gegangen." Wie antwortest du?
                                        13. Jemand erzählt dir von einem großen Verlust. Was würdest du dieser Person schreiben?
                                        14. Wie gratulierst du jemandem zu einem großen Erfolg? Schreib eine typische Nachricht.

                                        BLOCK 3 - MEINUNGEN & DISKUSSIONEN:
                                        15. Was ist ein Thema, über das du dich stundenlang unterhalten könntest? Erzähl mir davon.
                                        16. Nenne etwas, das die meisten Leute mögen, du aber überhaupt nicht verstehst. Warum?
                                        17. Jemand vertritt die komplett gegenteilige Meinung zu etwas, das dir wichtig ist. Wie reagierst du?
                                        18. Was würdest du gerne an der Welt verändern, wenn du könntest?
                                        19. Welchen Ratschlag würdest du deinem 16-jährigen Ich geben?

                                        BLOCK 4 - KREATIVITÄT & VORSTELLUNGSKRAFT:
                                        20. Stell dir vor, du könntest ein Jahr lang alles machen, was du willst, ohne finanzielle Sorgen. Was würdest du tun?
                                        21. Du findest eine Zeitmaschine. Wohin reist du und warum?
                                        22. Wenn du ein Buch über dein Leben schreiben würdest – wie würde der erste Satz lauten?
                                        23. Beschreibe deinen perfekten Tag von morgens bis abends.
                                        24. Welche drei Dinge würdest du auf eine einsame Insel mitnehmen? (außer Überlebenszeug)

                                        BLOCK 5 - BEZIEHUNGEN & SOZIALES:
                                        25. Beschreibe eine Person, die dir sehr wichtig ist, ohne ihren Namen zu nennen.
                                        26. Was schätzt du am meisten an deinen Freunden?
                                        27. Erzähl von einem Moment, in dem jemand dir wirklich geholfen hat.
                                        28. Wie würden deine besten Freunde dich in drei Sätzen beschreiben?
                                        29. Was bedeutet für dich "gute Kommunikation" in einer Beziehung?

                                        BLOCK 6 - ERINNERUNGEN & GESCHICHTEN:
                                        30. Was ist eine deiner Lieblings-Kindheitserinnerungen?
                                        31. Erzähl von einem Abenteuer oder einer Reise, die dich geprägt hat.
                                        32. Was war ein Wendepunkt in deinem Leben? Was hat sich dadurch verändert?
                                        33. Gibt es eine Geschichte, die du immer wieder gerne erzählst? Welche?
                                        34. Was war das beste Geschenk, das du je bekommen hast, und warum?

                                        BLOCK 7 - PRAKTISCHE SITUATIONEN:
                                        35. Du planst eine Überraschungsparty für jemanden. Beschreib, wie du das angehen würdest.
                                        36. Jemand bittet dich um Hilfe bei einem Umzug. Was antwortest du?
                                        37. Du hast einen Fehler gemacht, der jemand anderen betrifft. Wie entschuldigst du dich?
                                        38. Erkläre einem Kind, warum der Himmel blau ist.
                                        39. Du musst jemandem absagen, obwohl du zugesagt hattest. Was schreibst du?

                                        BLOCK 8 - SPONTANE REAKTIONEN:
                                        40. Vervollständige: "Wenn ich könnte, würde ich sofort..."
                                        41. Ein Wort, das dich beschreibt:
                                        42. Deine erste Reaktion, wenn etwas Unerwartetes passiert:
                                        43. Lieblings-Wort oder -Ausdruck:
                                        44. Was denkst du gerade in diesem Moment?
                                        45. Drei Dinge, für die du heute dankbar bist:
                                        46. Wenn du ein Tier wärst, welches und warum?
                                        47. Was machst du, wenn du gestresst bist?
                                        48. Dein Motto oder Lebensprinzip:
                                        49. Was bringt dich zum Lachen?
                                        50. Womit verbringst du am liebsten deine freie Zeit?

                                        BLOCK 9 - WERTE & PHILOSOPHISCHES:
                                        51. Was bedeutet für dich ein "gutes Leben"?
                                        52. Wofür würdest du kämpfen oder dich einsetzen?
                                        53. Was hoffst du, dass Menschen über dich sagen, wenn du nicht da bist?
                                        54. Gibt es etwas, das du bereust? Oder lebst du ohne Reue?
                                        55. Was ist dir wichtiger: geliebt zu werden oder respektiert zu werden? Warum?

                                        BLOCK 10 - ABSCHLUSSFRAGEN:
                                        56. Wenn du nur noch 24 Stunden zu leben hättest, was würdest du tun und wem würdest du was sagen?
                                        57. Was möchtest du den Menschen, die dir wichtig sind, auf jeden Fall noch sagen oder mitgeben?
                                        58. Gibt es etwas, das du schon immer mal loswerden wolltest, aber nie die Gelegenheit hattest?
                                        59. Wie würdest du dein bisheriges Leben in einem Satz zusammenfassen?
                                        60. Was macht dich zu DU?

                                        Höre aufmerksam zu, reagiere kurz auf die Antwort (1 Satz) und stelle dann die nächste Frage.
                                        Sei empathisch und interessiert. Gib ab Frage 20 gelegentlich kurze Ermutigungen wie "Du machst das großartig!" oder "Wir sind schon über die Hälfte!"
                                        """;

                                // German - Thank You State
                                thankYouMessage = """
                                        Vielen Dank für deine ausführlichen Antworten! 🎉

                                        Ich habe genügend Informationen über deinen Schreibstil und deine Persönlichkeit gesammelt.
                                        Alle deine Antworten wurden sicher gespeichert.

                                        Falls du möchtest, können wir gerne noch weitere Fragen durchgehen - ansonsten sind wir hier fertig.

                                        Möchtest du noch mehr Fragen beantworten, oder reicht das erstmal?
                                        """;

                                thankYouPrompt = "Du bedankst dich herzlich für die Teilnahme und fragst, ob die Person noch mehr erzählen möchte oder ob das reicht.";

                                break;

                        default:
                                // English (can be extended to IT, KO later)
                                consentStarter = """
                                        Welcome!

                                        This questionnaire is designed to capture your individual writing style and personality.
                                        Your answers will be used to train an AI model that can replicate your communication style.

                                        📋 Important Privacy Notes:
                                        • All data is used exclusively for research purposes
                                        • You can use pseudonyms or fake names
                                        • Demographic data is only recorded in broad categories

                                        Ready? Let's get started!

                                        Do you consent to your answers being used for research purposes (Bachelor thesis)? (Yes/No)
                                        """;

                                consentPrompt = "You are collecting consent and basic data for a research study. Ask the listed questions one by one, keeping responses brief and friendly.";
                                questionsStarter = "Perfect! Now for the detailed questions. Take your time with your answers. Let's begin:\n\nTell me about your day yesterday. What did you do?";
                                questionsPrompt = "You are an empathetic interviewer asking deep questions about writing style and personality. Ask ONE question at a time from the categories: daily life, emotions, opinions, creativity, relationships, and memories. Listen attentively and respond briefly before asking the next question.";
                                thankYouMessage = "Thank you for your detailed answers! I have collected enough information about your writing style and personality. Everything has been saved. Would you like to answer more questions, or is this enough?";
                                thankYouPrompt = "Thank the person warmly for participating and ask if they want to continue or if this is enough.";
                                break;
                }

                // Create States
                // State 1: Consent & Basic Info
                Decision consentTrigger = new StaticDecision(
                        "Has the user provided consent (answered 'yes' to research question) AND answered all basic questions (name, age, gender, messenger, message style, personality, communication style)? Answer 'yes' only if ALL are provided."
                );
                Decision consentGuard = new StaticDecision(
                        "Has the user explicitly refused consent or said 'no' to the research question? Answer 'yes' if they declined."
                );
                State questionsState = new State(questionsPrompt, "Deep Questions", questionsStarter, List.of());
                Transition toQuestions = new Transition(
                        List.of(consentTrigger),
                        List.of(),
                        questionsState
                );
                State consentState = new State(
                        consentPrompt,
                        "Consent & Basic Info",
                        consentStarter,
                        List.of(toQuestions)
                );

                // State 2: Deep Questions
                Decision questionsTrigger = new StaticDecision(
                        "Has the user answered at least 50-60 questions from all 10 blocks? Answer 'yes' only if comprehensive information across all categories has been collected."
                );
                Decision questionsGuard = new StaticDecision(
                        "Has the user indicated they want to stop or that they've shared enough? Answer 'yes' if they want to end."
                );
                State thankYouState = new State(
                        thankYouPrompt,
                        "Thank You",
                        thankYouMessage,
                        List.of()
                );
                Transition toThankYou = new Transition(
                        List.of(questionsTrigger, questionsGuard),
                        List.of(new StaticExtractionAction(
                                "Summarize the person's communication style, personality traits, and key characteristics based on all their answers. Include demographics and notable patterns.",
                                storage,
                                "profile_summary"
                        )),
                        thankYouState
                );

                // Add transition to questions state
                questionsState = new State(
                        questionsPrompt,
                        "Deep Questions",
                        questionsStarter,
                        List.of(toThankYou)
                );

                // Update the transition target
                toQuestions = new Transition(
                        List.of(consentTrigger),
                        List.of(),
                        questionsState
                );

                // Recreate consent state with updated transition
                consentState = new State(
                        consentPrompt,
                        "Consent & Basic Info",
                        consentStarter,
                        List.of(toQuestions)
                );

                // Create transition for refusal
                Transition toRefused = new Transition(
                        List.of(consentGuard),
                        List.of(),
                        new Final("User declined consent")
                );

                // Final consent state with both transitions
                consentState = new State(
                        consentPrompt,
                        "Consent & Basic Info",
                        consentStarter,
                        List.of(toQuestions, toRefused)
                );

                // Create Agent
                Agent result = new Agent(
                        data.getAgentName(),
                        data.getAgentDescription(),
                        consentState,
                        storage
                );

                // Set User-ID for multi-user tracking
                if (data.getUserId() != null && !data.getUserId().isBlank()) {
                        result.setUserId(data.getUserId());
                }

                // Don't call start() here - let the frontend call /start endpoint
                // Calling start() during creation causes the agent to advance a state
                // when the frontend later calls /start

                return result;
        }
}
