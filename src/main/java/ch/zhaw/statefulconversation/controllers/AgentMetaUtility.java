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
import ch.zhaw.statefulconversation.model.commons.actions.TransferUtterancesAction;
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

                if (data.getUserId() != null && !data.getUserId().isBlank()) {
                        result.setUserId(data.getUserId());
                }

                result.start();

                return result;
        }

        public static Agent createBiographerAgent(BiographerAgentCreateDTO data) {
                var storage = new Storage();
                String language = data.getLanguage() != null ? data.getLanguage() : "en";
                String nickname = data.getNickname();

                String triggerPrompt = buildTriggerPrompt(language);
                String[][] prompts = buildBlockPrompts(language, nickname);
                String[] blockNames = buildBlockNames(language);

                // Build chain backwards: Biography Complete ← Block10 ← ... ← Block1
                State current = new Final("Biography Complete");

                for (int i = 9; i >= 0; i--) {
                        State nextState = current;
                        String storageKey = "block" + (i + 1);

                        Decision trigger = new StaticDecision(triggerPrompt);
                        Decision guard = new StaticDecision(prompts[i][2]);
                        Action extract = new StaticExtractionAction(prompts[i][3], storage, storageKey);
                        Action transfer = new TransferUtterancesAction(nextState);
                        Transition t = new Transition(List.of(trigger, guard), List.of(extract, transfer), nextState);

                        current = new State(prompts[i][0], blockNames[i], prompts[i][1], List.of(t));
                }

                Agent result = new Agent(data.getAgentName(), data.getAgentDescription(), current, storage);

                if (data.getUserId() != null && !data.getUserId().isBlank()) {
                        result.setUserId(data.getUserId());
                }

                result.start();

                return result;
        }

        // ============================================================
        // TRIGGER PROMPT (universal exit condition per language)
        // ============================================================

        private static String buildTriggerPrompt(String language) {
                switch (language) {
                        case "de":
                                return "Hat die Person ausdrücklich signalisiert, das Gespräch zu beenden oder zu pausieren (z.B. 'Pause', 'genug', 'aufhören', 'ich bin fertig')? Antworte nur 'yes' oder 'no'.";
                        case "it":
                                return "La persona ha esplicitamente segnalato di voler terminare o mettere in pausa la conversazione? Rispondi solo 'yes' o 'no'.";
                        case "ko":
                                return "상대방이 대화를 끝내거나 중단하겠다고 명시적으로 표현했습니까? 'yes' 또는 'no'로만 답하세요.";
                        default:
                                return "Has the person explicitly signaled they want to end or pause the conversation (e.g. 'stop', 'enough', 'I'm done', 'break')? Answer only 'yes' or 'no'.";
                }
        }

        // ============================================================
        // BLOCK NAMES (state names shown in API response)
        // ============================================================

        private static String[] buildBlockNames(String language) {
                switch (language) {
                        case "de":
                                return new String[] {
                                                "Block 1 - Alltag",
                                                "Block 2 - Emotionen",
                                                "Block 3 - Erinnerungen",
                                                "Block 4 - Beziehungen",
                                                "Block 5 - Wachstum",
                                                "Block 6 - Träume",
                                                "Block 7 - Werte",
                                                "Block 8 - Vermächtnis",
                                                "Block 9 - Botschaften",
                                                "Block 10 - Abschluss"
                                };
                        case "it":
                                return new String[] {
                                                "Block 1 - Quotidiano",
                                                "Block 2 - Emozioni",
                                                "Block 3 - Ricordi",
                                                "Block 4 - Relazioni",
                                                "Block 5 - Crescita",
                                                "Block 6 - Sogni",
                                                "Block 7 - Valori",
                                                "Block 8 - Eredità",
                                                "Block 9 - Messaggi",
                                                "Block 10 - Conclusione"
                                };
                        case "ko":
                                return new String[] {
                                                "Block 1 - 일상",
                                                "Block 2 - 감정",
                                                "Block 3 - 추억",
                                                "Block 4 - 관계",
                                                "Block 5 - 성장",
                                                "Block 6 - 꿈",
                                                "Block 7 - 가치관",
                                                "Block 8 - 유산",
                                                "Block 9 - 메시지",
                                                "Block 10 - 마무리"
                                };
                        default:
                                return new String[] {
                                                "Block 1 - Daily Life",
                                                "Block 2 - Emotions",
                                                "Block 3 - Memories",
                                                "Block 4 - Relationships",
                                                "Block 5 - Growth",
                                                "Block 6 - Dreams",
                                                "Block 7 - Values",
                                                "Block 8 - Legacy",
                                                "Block 9 - Messages",
                                                "Block 10 - Closing"
                                };
                }
        }

        // ============================================================
        // BLOCK PROMPTS DISPATCHER
        // Returns [10][4]: [blockIdx][0=state, 1=starter, 2=guard, 3=extract]
        // ============================================================

        private static String[][] buildBlockPrompts(String language, String nickname) {
                String[][] result = new String[10][4];
                switch (language) {
                        case "de":
                                fillDEPrompts(result, nickname);
                                break;
                        case "it":
                                fillITPrompts(result, nickname);
                                break;
                        case "ko":
                                fillKOPrompts(result, nickname);
                                break;
                        default:
                                fillENPrompts(result, nickname);
                                break;
                }
                return result;
        }

        // ============================================================
        // GERMAN PROMPTS
        // ============================================================

        private static void fillDEPrompts(String[][] p, String nickname) {
                String nameNote = (nickname != null && !nickname.isBlank())
                                ? " Du weißt, dass die Person " + nickname
                                                + " heißt. Sprich sie gelegentlich mit diesem Namen an."
                                : "";

                // Block 1 - Alltag
                p[0][0] = "WICHTIG: Kommuniziere ausschließlich auf Deutsch. Du bist ein einfühlsamer Biograf."
                                + nameNote
                                + " Erkunde jetzt den Alltag der Person: Morgenroutine, Tagesstruktur (Arbeit/Schule/Hobbys), Lieblingsessen, Abendroutine, Wochenendgestaltung. Stelle jeweils eine Frage, höre aufmerksam zu. Antworten kurz (1-2 Sätze).";
                p[0][1] = "Schreibe eine kurze, herzliche Begrüßung auf Deutsch (max. 2 Sätze). Keine Selbstvorstellung mit eigenem Namen. Frage direkt nach der Morgenroutine oder dem typischen Alltag.";
                p[0][2] = "Wurden mindestens 4-5 Alltagsthemen besprochen, z.B. Morgenroutine, Arbeit oder Hobbys, Essen, Abend und Wochenende? Antworte nur 'yes' oder 'no'.";
                p[0][3] = "Fasse den Alltag der Person zusammen: Morgenroutine, Tagesstruktur, Hobbys, Essgewohnheiten, Abend- und Wochenendgestaltung. Persönlich und präzise.";

                // Block 2 - Emotionen
                p[1][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und erkundest jetzt die emotionale Welt der Person. Frage nach: glücklichen Momenten, was sie stresst oder besorgt, wie sie Zuneigung ausdrückt, wie sie mit Ärger umgeht, und was sie zum Lachen bringt. Jeweils eine Frage, 1-2 Sätze.";
                p[1][1] = "Schreibe auf Deutsch eine einfühlsame Überleitung von Alltagsthemen hin zu Emotionen und inneren Erlebnissen. Lade die Person ein, über ihre emotionale Welt zu sprechen.";
                p[1][2] = "Wurden mindestens 4-5 emotionale Themen besprochen, z.B. Glücksmomente, Stress, Zuneigung, Ärger und Humor? Antworte nur 'yes' oder 'no'.";
                p[1][3] = "Fasse die emotionale Welt der Person zusammen: was sie glücklich macht, was sie besorgt, wie sie Gefühle ausdrückt, Umgang mit negativen Emotionen, Humor.";

                // Block 3 - Erinnerungen
                p[2][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und reist jetzt mit der Person in ihre Vergangenheit. Frage nach: frühester Kindheitserinnerung, schönster Kindheitserinnerung, Schulanfang, einem prägenden Erlebnis, einem lustigen oder peinlichen Moment. Jeweils eine Frage, 1-2 Sätze.";
                p[2][1] = "Schreibe auf Deutsch eine einladende Überleitung zum Thema Erinnerungen und Kindheit. Der Biograf möchte die Person auf eine Reise in die Vergangenheit mitnehmen.";
                p[2][2] = "Wurden mindestens 4-5 Erinnerungen besprochen, z.B. Kindheit, Schulzeit, prägende Erlebnisse oder besondere Momente? Antworte nur 'yes' oder 'no'.";
                p[2][3] = "Fasse die wichtigsten Erinnerungen der Person zusammen: Kindheitserinnerungen, Schulzeit, prägende Erlebnisse und besondere Momente.";

                // Block 4 - Beziehungen
                p[3][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und erkundest jetzt die Beziehungen der Person. Frage nach: der wichtigsten Person in ihrem Leben und warum, Familiendynamik, bester Freundschaftsgeschichte, wie sie sich in Beziehungen verhält, und wen sie in einer Krise anruft. Jeweils eine Frage, 1-2 Sätze.";
                p[3][1] = "Schreibe auf Deutsch eine warme Überleitung zum Thema Beziehungen und wichtige Menschen im Leben. Lade die Person ein, über ihre bedeutsamsten Verbindungen zu sprechen.";
                p[3][2] = "Wurden mindestens 4-5 Beziehungsthemen besprochen, z.B. wichtigste Person, Familie, Freundschaft, Beziehungsverhalten und Krisenunterstützung? Antworte nur 'yes' oder 'no'.";
                p[3][3] = "Fasse die Beziehungswelt der Person zusammen: wichtigste Menschen, Familiendynamik, Freundschaften, Beziehungsmuster und soziales Netz.";

                // Block 5 - Wachstum
                p[4][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und erkundest jetzt das persönliche Wachstum der Person. Frage nach: wichtigster Lebenslehre, größtem Fehler und was sie daraus gelernt hat, größtem Erfolg, wie sie sich in den letzten Jahren verändert hat, und was sie noch lernt. Jeweils eine Frage, 1-2 Sätze.";
                p[4][1] = "Schreibe auf Deutsch eine inspirierende Überleitung zum Thema persönliches Wachstum und Entwicklung. Der Biograf möchte die Lebensreise der Person erkunden.";
                p[4][2] = "Wurden mindestens 4-5 Wachstumsthemen besprochen, z.B. Lebenslehren, Fehler, Erfolge, persönliche Veränderung und Lernen? Antworte nur 'yes' oder 'no'.";
                p[4][3] = "Fasse das persönliche Wachstum der Person zusammen: wichtigste Lebenslektionen, Fehler und Erkenntnisse, Erfolge und persönliche Entwicklung.";

                // Block 6 - Träume
                p[5][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und erkundest jetzt die Träume der Person. Frage nach: Kindheitstraum-Beruf, Traumziel oder Traumreise, einem Zukunftstraum, einem Traum den sie aufgegeben haben, und einem Traum der noch lebt. Jeweils eine Frage, 1-2 Sätze.";
                p[5][1] = "Schreibe auf Deutsch eine träumerische Überleitung zum Thema Träume und Wünsche. Der Biograf möchte in die Traumwelt der Person eintauchen.";
                p[5][2] = "Wurden mindestens 4-5 Traumthemen besprochen, z.B. Kindheitstraum, Traumreise, Zukunft, aufgegebene oder noch lebendige Träume? Antworte nur 'yes' oder 'no'.";
                p[5][3] = "Fasse die Träume und Wünsche der Person zusammen: Kindheitsträume, Reiseträume, Zukunftsvisionen, aufgegebene und noch lebendige Träume.";

                // Block 7 - Werte
                p[6][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und erkundest jetzt die Werte der Person. Frage nach: ihren wichtigsten Werten, Vorbildern, was einen guten Menschen ausmacht, wofür sie eintreten, und was sie niemals kompromittieren würden. Jeweils eine Frage, 1-2 Sätze.";
                p[6][1] = "Schreibe auf Deutsch eine nachdenkliche Überleitung zum Thema Werte und Überzeugungen. Der Biograf möchte verstehen, was der Person wirklich wichtig ist.";
                p[6][2] = "Wurden mindestens 4-5 Werthemen besprochen, z.B. Werte, Vorbilder, guter Mensch, Überzeugungen und Prinzipien? Antworte nur 'yes' oder 'no'.";
                p[6][3] = "Fasse die Werte und Überzeugungen der Person zusammen: Kernwerte, Vorbilder, ethische Prinzipien und wofür sie eintreten.";

                // Block 8 - Vermächtnis
                p[7][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und erkundest jetzt das Vermächtnis der Person. Frage nach: woran sie erinnert werden möchte, Rat an zukünftige Generationen, ihrem wichtigsten persönlichen Beitrag, wie andere sich bei der Erinnerung an sie fühlen sollen, und ihrem Lebensmotto. Jeweils eine Frage, 1-2 Sätze.";
                p[7][1] = "Schreibe auf Deutsch eine tiefgründige Überleitung zum Thema Vermächtnis. Was möchte die Person hinterlassen und wie möchte sie erinnert werden?";
                p[7][2] = "Wurden mindestens 4-5 Vermächtnisthemen besprochen, z.B. Erinnerung, Rat, Beitrag, Gefühle anderer oder Lebensmotto? Antworte nur 'yes' oder 'no'.";
                p[7][3] = "Fasse die Vermächtnisgedanken der Person zusammen: wie sie erinnert werden möchte, ihr Rat an andere, ihr wichtigster Beitrag und ihr Lebensmotto.";

                // Block 9 - Botschaften
                p[8][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und erfasst jetzt persönliche Botschaften der Person. Frage nach: einem Brief an ihr jüngeres Ich, einer Botschaft an die beste Freundin oder den besten Freund, einer Botschaft an die Familie, einer Botschaft an jemanden mit dem sie den Kontakt verloren haben, und einer Botschaft an die nächste Generation. Jeweils eine Frage.";
                p[8][1] = "Schreibe auf Deutsch eine emotionale Überleitung zum Thema persönliche Botschaften. Der Biograf möchte die Worte der Person für wichtige Menschen in ihrem Leben festhalten.";
                p[8][2] = "Wurden mindestens 4-5 Botschaften besprochen, z.B. Brief ans jüngere Ich, Freunde, Familie, verlorene Kontakte oder nächste Generation? Antworte nur 'yes' oder 'no'.";
                p[8][3] = "Fasse die persönlichen Botschaften der Person zusammen: Brief ans jüngere Ich, Botschaften an Freunde, Familie, verlorene Kontakte und die nächste Generation.";

                // Block 10 - Abschluss
                p[9][0] = "WICHTIG: Kommuniziere auf Deutsch. Du bist Biograf und schließt das Gespräch jetzt würdevoll ab. Frage nach: wofür die Person am dankbarsten ist, was sie anders machen würde, worauf sie am stolzesten ist, ihren letzten Gedanken, und Abschiedsworten für ihr digitales Erbe. Nimm dir Zeit für jeden Punkt. Schließe das Gespräch warm ab.";
                p[9][1] = "Schreibe auf Deutsch eine würdevolle Abschlussüberleitung. Der Biograf möchte das Gespräch mit den abschließenden Gedanken und einem Abschiedswort der Person beenden.";
                p[9][2] = "Wurden mindestens 4-5 Abschlussthemen besprochen, z.B. Dankbarkeit, Bedauern, Stolz, letzte Gedanken oder Abschiedsworte? Antworte nur 'yes' oder 'no'.";
                p[9][3] = "Fasse die Abschlussgedanken der Person zusammen: Dankbarkeit, was sie anders machen würde, größte Erfolge, letzte Weisheiten und Abschiedsworte für ihr digitales Erbe.";
        }

        // ============================================================
        // ENGLISH PROMPTS
        // ============================================================

        private static void fillENPrompts(String[][] p, String nickname) {
                String nameNote = (nickname != null && !nickname.isBlank())
                                ? " You know this person's name is " + nickname + ". Address them by name occasionally."
                                : "";

                // Block 1 - Daily Life
                p[0][0] = "You are an empathetic biographer." + nameNote
                                + " Explore daily life: morning routine, work/school/hobbies, favorite foods, evening routine, weekends. Ask one question at a time. Keep responses brief (1-2 sentences).";
                p[0][1] = "Write a brief, warm opening in English (max. 2 sentences). No self-introduction with a name. Ask directly about their morning routine or typical day.";
                p[0][2] = "Have at least 4-5 daily life topics been discussed, e.g. morning routine, work, food, evenings, weekends? Answer only 'yes' or 'no'.";
                p[0][3] = "Summarize the person's daily life: morning routine, work/activities, hobbies, eating habits, evening and weekend routines.";

                // Block 2 - Emotions
                p[1][0] = "You are an empathetic biographer. Explore emotions: happy moments, stress and worries, how they express love, handling anger, what makes them laugh. One question at a time, brief responses.";
                p[1][1] = "Write a warm transition in English from daily life to emotions and inner experience. Invite the person to share their emotional world.";
                p[1][2] = "Have at least 4-5 emotional topics been discussed, e.g. happiness, stress, affection, anger, humor? Answer only 'yes' or 'no'.";
                p[1][3] = "Summarize the person's emotional world: what makes them happy, worries, how they express feelings, handling negative emotions, sense of humor.";

                // Block 3 - Memories
                p[2][0] = "You are an empathetic biographer. Explore memories: earliest childhood memory, favorite childhood memory, first day of school, a formative event, a funny or embarrassing moment. One question at a time.";
                p[2][1] = "Write a gentle transition in English to memories and the past. Invite the person on a journey through their history.";
                p[2][2] = "Have at least 4-5 memories been discussed, e.g. childhood, school days, formative events, memorable moments? Answer only 'yes' or 'no'.";
                p[2][3] = "Summarize key memories: childhood experiences, school days, formative events and memorable moments.";

                // Block 4 - Relationships
                p[3][0] = "You are an empathetic biographer. Explore relationships: most important person in their life and why, family dynamics, best friendship story, how they behave in relationships, who they'd call in crisis. One question at a time.";
                p[3][1] = "Write a warm transition in English to relationships and the important people in their life.";
                p[3][2] = "Have at least 4-5 relationship topics been discussed, e.g. key person, family, friendship, behavior in relationships, crisis support? Answer only 'yes' or 'no'.";
                p[3][3] = "Summarize the person's relationship world: key people, family dynamics, friendships, relationship patterns and social network.";

                // Block 5 - Growth
                p[4][0] = "You are an empathetic biographer. Explore personal growth: most important life lesson, biggest mistake and what they learned, greatest success, how they've changed over the years, what they're still learning. One question at a time.";
                p[4][1] = "Write an inspiring transition in English to personal growth and development.";
                p[4][2] = "Have at least 4-5 growth topics been discussed, e.g. life lessons, mistakes, successes, personal change, learning? Answer only 'yes' or 'no'.";
                p[4][3] = "Summarize personal growth: key life lessons, mistakes and insights, successes and personal development journey.";

                // Block 6 - Dreams
                p[5][0] = "You are an empathetic biographer. Explore dreams: childhood career dream, dream destination, a future dream, a dream they gave up, a dream still alive today. One question at a time.";
                p[5][1] = "Write a dreamy transition in English to explore dreams and aspirations.";
                p[5][2] = "Have at least 4-5 dream topics been discussed, e.g. childhood dream, travel, future, abandoned or living dreams? Answer only 'yes' or 'no'.";
                p[5][3] = "Summarize dreams and aspirations: childhood dreams, travel wishes, future visions, abandoned and still-living dreams.";

                // Block 7 - Values
                p[6][0] = "You are an empathetic biographer. Explore values: most important values, role models, what makes a good person, what they stand for, what they'd never compromise. One question at a time.";
                p[6][1] = "Write a thoughtful transition in English to values and beliefs. What truly matters to this person?";
                p[6][2] = "Have at least 4-5 values topics been discussed, e.g. core values, role models, good person, beliefs, principles? Answer only 'yes' or 'no'.";
                p[6][3] = "Summarize values and beliefs: core values, role models, ethical principles, what they stand for.";

                // Block 8 - Legacy
                p[7][0] = "You are an empathetic biographer. Explore legacy: how they want to be remembered, advice to future generations, most important personal contribution, how they want others to feel when thinking of them, their life motto. One question at a time.";
                p[7][1] = "Write a profound transition in English to legacy and what they want to leave behind for others.";
                p[7][2] = "Have at least 4-5 legacy topics been discussed, e.g. memory, advice, contribution, feelings in others, life motto? Answer only 'yes' or 'no'.";
                p[7][3] = "Summarize legacy thoughts: how they want to be remembered, advice to others, most important contribution and life motto.";

                // Block 9 - Messages
                p[8][0] = "You are an empathetic biographer. Capture personal messages: a letter to their younger self, a message to their best friend, a message to their family, a message to someone they lost touch with, a message to the next generation. One question at a time.";
                p[8][1] = "Write an emotional transition in English to personal messages. The biographer wants to capture their words for the important people in their life.";
                p[8][2] = "Have at least 4-5 message topics been discussed, e.g. younger self, friend, family, lost contact, next generation? Answer only 'yes' or 'no'.";
                p[8][3] = "Summarize personal messages: letter to younger self, messages to best friend, family, lost contacts and future generations.";

                // Block 10 - Closing
                p[9][0] = "You are an empathetic biographer closing the conversation with dignity. Ask about: what they're most grateful for, what they'd do differently, what they're most proud of, their final thoughts, and closing words for their digital legacy. Take time with each question.";
                p[9][1] = "Write a dignified closing transition in English. The biographer wants to conclude the biography with final reflections and farewell words for their digital legacy.";
                p[9][2] = "Have at least 4-5 closing topics been discussed, e.g. gratitude, regrets, pride, final thoughts, farewell words? Answer only 'yes' or 'no'.";
                p[9][3] = "Summarize closing reflections: gratitude, what they'd do differently, greatest pride, final thoughts and farewell words for their digital legacy.";
        }

        // ============================================================
        // ITALIAN PROMPTS
        // ============================================================

        private static void fillITPrompts(String[][] p, String nickname) {
                String nameNote = (nickname != null && !nickname.isBlank())
                                ? " Sai che questa persona si chiama " + nickname
                                                + ". Rivolgiti a lei con questo nome di tanto in tanto."
                                : "";

                // Block 1 - Quotidiano
                p[0][0] = "IMPORTANTE: Comunica esclusivamente in italiano. Sei un biografo empatico." + nameNote
                                + " Esplora la vita quotidiana: routine mattutina, struttura della giornata (lavoro/scuola/hobby), cibo preferito, routine serale, fine settimana. Una domanda alla volta, risposte brevi (1-2 frasi).";
                p[0][1] = "Scrivi un breve benvenuto in italiano (max. 2 frasi). Nessuna autopresentazione con un nome. Chiedi direttamente della routine mattutina o della giornata tipica.";
                p[0][2] = "Sono stati discussi almeno 4-5 aspetti della vita quotidiana, es. routine mattutina, lavoro, cibo, sera, fine settimana? Rispondi solo 'yes' o 'no'.";
                p[0][3] = "Riassumi la vita quotidiana della persona: routine mattutina, attività giornaliere, hobby, abitudini alimentari, serate e fine settimana.";

                // Block 2 - Emozioni
                p[1][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e esplori ora il mondo emotivo della persona. Chiedi di: momenti felici, stress e preoccupazioni, come esprime affetto, come gestisce la rabbia, cosa la fa ridere. Una domanda alla volta, 1-2 frasi.";
                p[1][1] = "Scrivi in italiano una transizione empatica dalla vita quotidiana alle emozioni. Invita la persona a parlare del suo mondo interiore.";
                p[1][2] = "Sono stati discussi almeno 4-5 temi emotivi, es. momenti felici, stress, affetto, rabbia, umorismo? Rispondi solo 'yes' o 'no'.";
                p[1][3] = "Riassumi il mondo emotivo della persona: cosa la rende felice, preoccupazioni, come esprime i sentimenti, gestione delle emozioni negative, senso dell'umorismo.";

                // Block 3 - Ricordi
                p[2][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e esplori ora i ricordi della persona. Chiedi del: primo ricordo d'infanzia, ricordo preferito dell'infanzia, primo giorno di scuola, un evento formativo, un momento divertente o imbarazzante. Una domanda alla volta.";
                p[2][1] = "Scrivi in italiano una transizione gentile ai ricordi e all'infanzia. Invita la persona in un viaggio nel passato.";
                p[2][2] = "Sono stati discussi almeno 4-5 ricordi, es. infanzia, scuola, eventi formativi, momenti memorabili? Rispondi solo 'yes' o 'no'.";
                p[2][3] = "Riassumi i ricordi più importanti: esperienze d'infanzia, anni scolastici, eventi formativi e momenti memorabili.";

                // Block 4 - Relazioni
                p[3][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e esplori ora le relazioni della persona. Chiedi di: la persona più importante nella sua vita e perché, dinamiche familiari, la migliore storia di amicizia, come si comporta nelle relazioni, chi chiamerebbe in una crisi. Una domanda alla volta.";
                p[3][1] = "Scrivi in italiano una transizione calorosa alle relazioni e alle persone importanti nella vita.";
                p[3][2] = "Sono stati discussi almeno 4-5 temi relazionali, es. persona chiave, famiglia, amicizia, comportamento, supporto in crisi? Rispondi solo 'yes' o 'no'.";
                p[3][3] = "Riassumi il mondo relazionale della persona: persone chiave, dinamiche familiari, amicizie, modelli relazionali e rete sociale.";

                // Block 5 - Crescita
                p[4][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e esplori ora la crescita personale. Chiedi di: la lezione di vita più importante, il più grande errore e cosa ha imparato, il più grande successo, come è cambiata negli anni, cosa sta ancora imparando. Una domanda alla volta.";
                p[4][1] = "Scrivi in italiano una transizione ispiratrice alla crescita personale e allo sviluppo.";
                p[4][2] = "Sono stati discussi almeno 4-5 temi di crescita, es. lezioni di vita, errori, successi, cambiamento, apprendimento? Rispondi solo 'yes' o 'no'.";
                p[4][3] = "Riassumi la crescita personale: lezioni di vita chiave, errori e intuizioni, successi e percorso di sviluppo.";

                // Block 6 - Sogni
                p[5][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e esplori ora i sogni della persona. Chiedi del: sogno di lavoro da bambino, meta o viaggio dei sogni, un sogno futuro, un sogno abbandonato, un sogno ancora vivo. Una domanda alla volta.";
                p[5][1] = "Scrivi in italiano una transizione sognante ai sogni e ai desideri.";
                p[5][2] = "Sono stati discussi almeno 4-5 temi di sogni, es. sogno infantile, viaggio, futuro, sogni abbandonati o ancora vivi? Rispondi solo 'yes' o 'no'.";
                p[5][3] = "Riassumi i sogni e le aspirazioni: sogni d'infanzia, desideri di viaggio, visioni future, sogni abbandonati e ancora vivi.";

                // Block 7 - Valori
                p[6][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e esplori ora i valori della persona. Chiedi dei: valori più importanti, modelli di riferimento, cosa rende una buona persona, cosa difende, cosa non comprometterebbe mai. Una domanda alla volta.";
                p[6][1] = "Scrivi in italiano una transizione riflessiva ai valori e alle convinzioni. Cosa conta davvero per questa persona?";
                p[6][2] = "Sono stati discussi almeno 4-5 temi di valori, es. valori fondamentali, modelli, buona persona, convinzioni, principi? Rispondi solo 'yes' o 'no'.";
                p[6][3] = "Riassumi i valori e le convinzioni: valori fondamentali, modelli di riferimento, principi etici e cosa difende.";

                // Block 8 - Eredità
                p[7][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e esplori ora l'eredità della persona. Chiedi di: come vuole essere ricordata, consigli per le generazioni future, il contributo personale più importante, come vuole che gli altri si sentano pensando a lei, il suo motto di vita. Una domanda alla volta.";
                p[7][1] = "Scrivi in italiano una transizione profonda all'eredità e a ciò che vuole lasciare agli altri.";
                p[7][2] = "Sono stati discussi almeno 4-5 temi di eredità, es. memoria, consiglio, contributo, sentimenti degli altri, motto di vita? Rispondi solo 'yes' o 'no'.";
                p[7][3] = "Riassumi i pensieri sull'eredità: come vuole essere ricordata, consigli, contributo più importante e motto di vita.";

                // Block 9 - Messaggi
                p[8][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e raccogli messaggi personali. Chiedi di: una lettera al sé più giovane, un messaggio al migliore amico, un messaggio alla famiglia, un messaggio a qualcuno con cui ha perso i contatti, un messaggio alla prossima generazione. Una domanda alla volta.";
                p[8][1] = "Scrivi in italiano una transizione emotiva ai messaggi personali. Il biografo vuole fissare le parole della persona per le persone importanti della sua vita.";
                p[8][2] = "Sono stati discussi almeno 4-5 messaggi, es. sé più giovane, amico, famiglia, contatto perso, prossima generazione? Rispondi solo 'yes' o 'no'.";
                p[8][3] = "Riassumi i messaggi personali: lettera al sé più giovane, messaggi al migliore amico, famiglia, contatti persi e generazioni future.";

                // Block 10 - Conclusione
                p[9][0] = "IMPORTANTE: Comunica in italiano. Sei il biografo e chiudi la conversazione con dignità. Chiedi di: cosa è più grata, cosa farebbe diversamente, di cosa è più orgogliosa, i pensieri finali, e le parole di commiato per la sua eredità digitale. Prenditi tempo con ogni domanda.";
                p[9][1] = "Scrivi in italiano una transizione conclusiva dignitosa. Il biografo vuole concludere con riflessioni finali e parole di commiato per l'eredità digitale.";
                p[9][2] = "Sono stati discussi almeno 4-5 temi conclusivi, es. gratitudine, rimpianti, orgoglio, pensieri finali, commiato? Rispondi solo 'yes' o 'no'.";
                p[9][3] = "Riassumi le riflessioni conclusive: gratitudine, cosa farebbe diversamente, maggiore orgoglio, pensieri finali e parole di commiato per l'eredità digitale.";
        }

        // ============================================================
        // KOREAN PROMPTS
        // ============================================================

        private static void fillKOPrompts(String[][] p, String nickname) {
                String nameNote = (nickname != null && !nickname.isBlank())
                                ? " 당신은 이 사람의 이름이 " + nickname + "임을 알고 있습니다. 대화 중 가끔 이 이름으로 불러주세요."
                                : "";

                // Block 1 - 일상
                p[0][0] = "중요: 한국어로만 소통하세요. 당신은 공감적인 전기 작가입니다." + nameNote
                                + " 일상에 대해 탐구하세요: 아침 루틴, 하루 구조(직장/학교/취미), 좋아하는 음식, 저녁 루틴, 주말 활동. 한 번에 하나의 질문만 하고 짧게 답변하세요(1-2문장).";
                p[0][1] = "한국어로 짧고 따뜻한 인사말을 작성하세요 (최대 2문장). 이름으로 자기소개 없이 바로 아침 루틴이나 일상에 대해 질문하세요.";
                p[0][2] = "일상의 4-5가지 주제(아침 루틴, 직장, 음식, 저녁, 주말 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[0][3] = "사람의 일상을 요약하세요: 아침 루틴, 일과, 취미, 식습관, 저녁과 주말 활동.";

                // Block 2 - 감정
                p[1][0] = "중요: 한국어로 소통하세요. 이제 감정 세계를 탐구합니다: 행복한 순간, 스트레스와 걱정, 애정 표현 방식, 화 다루는 법, 웃음을 주는 것. 한 번에 하나의 질문, 1-2문장 답변.";
                p[1][1] = "한국어로 일상에서 감정과 내면 경험으로 이어지는 따뜻한 전환 문구를 작성하세요.";
                p[1][2] = "감정 주제 4-5가지(행복, 스트레스, 애정, 분노, 유머 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[1][3] = "사람의 감정 세계를 요약하세요: 행복 요인, 걱정, 감정 표현 방식, 부정적 감정 처리, 유머 감각.";

                // Block 3 - 추억
                p[2][0] = "중요: 한국어로 소통하세요. 이제 추억을 탐구합니다: 가장 오래된 어린 시절 기억, 좋아하는 어린 시절 기억, 첫 등교일, 형성적 사건, 재미있거나 당황스러운 순간. 한 번에 하나의 질문.";
                p[2][1] = "한국어로 추억과 과거로의 부드러운 전환 문구를 작성하세요. 사람을 과거 여행으로 초대하세요.";
                p[2][2] = "추억 4-5가지(어린 시절, 학교, 형성적 사건, 기억에 남는 순간 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[2][3] = "주요 추억을 요약하세요: 어린 시절 경험, 학창 시절, 형성적 사건과 기억에 남는 순간들.";

                // Block 4 - 관계
                p[3][0] = "중요: 한국어로 소통하세요. 이제 관계를 탐구합니다: 삶에서 가장 중요한 사람과 이유, 가족 역학, 최고의 우정 이야기, 관계에서의 행동 방식, 위기 시 연락할 사람. 한 번에 하나의 질문.";
                p[3][1] = "한국어로 관계와 삶의 중요한 사람들로의 따뜻한 전환 문구를 작성하세요.";
                p[3][2] = "관계 주제 4-5가지(핵심 인물, 가족, 우정, 관계 행동, 위기 지원 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[3][3] = "관계 세계를 요약하세요: 핵심 인물, 가족 역학, 우정, 관계 패턴과 사회적 네트워크.";

                // Block 5 - 성장
                p[4][0] = "중요: 한국어로 소통하세요. 이제 개인 성장을 탐구합니다: 가장 중요한 삶의 교훈, 가장 큰 실수와 배운 점, 가장 큰 성공, 몇 년간의 변화, 아직 배우고 있는 것. 한 번에 하나의 질문.";
                p[4][1] = "한국어로 개인 성장과 발전으로의 영감을 주는 전환 문구를 작성하세요.";
                p[4][2] = "성장 주제 4-5가지(삶의 교훈, 실수, 성공, 변화, 학습 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[4][3] = "개인 성장을 요약하세요: 핵심 삶의 교훈, 실수와 통찰, 성공과 성장 여정.";

                // Block 6 - 꿈
                p[5][0] = "중요: 한국어로 소통하세요. 이제 꿈을 탐구합니다: 어린 시절 꿈의 직업, 꿈의 목적지, 미래의 꿈, 포기한 꿈, 아직 살아있는 꿈. 한 번에 하나의 질문.";
                p[5][1] = "한국어로 꿈과 소망으로의 환상적인 전환 문구를 작성하세요.";
                p[5][2] = "꿈 주제 4-5가지(어린 시절 꿈, 여행, 미래, 포기한 꿈, 현재 꿈 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[5][3] = "꿈과 열망을 요약하세요: 어린 시절 꿈, 여행 소망, 미래 비전, 포기한 꿈과 아직 살아있는 꿈.";

                // Block 7 - 가치관
                p[6][0] = "중요: 한국어로 소통하세요. 이제 가치관을 탐구합니다: 가장 중요한 가치, 롤모델, 좋은 사람이란 무엇인가, 무엇을 위해 싸우는가, 절대 타협하지 않을 것. 한 번에 하나의 질문.";
                p[6][1] = "한국어로 가치관과 신념으로의 사려 깊은 전환 문구를 작성하세요. 이 사람에게 진정으로 중요한 것은 무엇인가요?";
                p[6][2] = "가치관 주제 4-5가지(핵심 가치, 롤모델, 좋은 사람, 신념, 원칙 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[6][3] = "가치관과 신념을 요약하세요: 핵심 가치, 롤모델, 윤리적 원칙, 무엇을 위해 싸우는가.";

                // Block 8 - 유산
                p[7][0] = "중요: 한국어로 소통하세요. 이제 유산을 탐구합니다: 어떻게 기억되고 싶은가, 미래 세대에 대한 조언, 가장 중요한 개인적 기여, 자신을 생각할 때 타인이 어떤 감정을 느끼기를 바라는가, 삶의 모토. 한 번에 하나의 질문.";
                p[7][1] = "한국어로 유산과 남기고 싶은 것으로의 깊이 있는 전환 문구를 작성하세요.";
                p[7][2] = "유산 주제 4-5가지(기억, 조언, 기여, 타인의 감정, 삶의 모토 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[7][3] = "유산 생각을 요약하세요: 어떻게 기억되고 싶은가, 조언, 가장 중요한 기여, 삶의 모토.";

                // Block 9 - 메시지
                p[8][0] = "중요: 한국어로 소통하세요. 이제 개인 메시지를 담습니다: 어린 시절 자신에게 보내는 편지, 가장 친한 친구에게, 가족에게, 연락이 끊긴 사람에게, 다음 세대에게. 한 번에 하나의 질문.";
                p[8][1] = "한국어로 개인 메시지로의 감동적인 전환 문구를 작성하세요. 삶의 중요한 사람들을 위한 메시지를 담겠습니다.";
                p[8][2] = "메시지 주제 4-5가지(어린 자신, 친구, 가족, 연락 끊긴 사람, 다음 세대 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[8][3] = "개인 메시지를 요약하세요: 어린 자신에게, 가장 친한 친구에게, 가족에게, 연락 끊긴 사람에게, 미래 세대에게.";

                // Block 10 - 마무리
                p[9][0] = "중요: 한국어로 소통하세요. 이제 대화를 품위 있게 마무리합니다. 물어보세요: 가장 감사한 것, 다르게 했을 것, 가장 자랑스러운 것, 마지막 생각, 디지털 유산을 위한 작별 인사. 각 질문에 충분한 시간을 주세요.";
                p[9][1] = "한국어로 품위 있는 마무리 전환 문구를 작성하세요. 마지막 성찰과 디지털 유산을 위한 작별 인사로 마무리합니다.";
                p[9][2] = "마무리 주제 4-5가지(감사, 아쉬움, 자부심, 마지막 생각, 작별 인사 등)가 논의되었나요? 'yes' 또는 'no'로만 답하세요.";
                p[9][3] = "마무리 성찰을 요약하세요: 감사함, 다르게 했을 것, 가장 큰 자부심, 마지막 생각과 디지털 유산을 위한 작별 인사.";
        }
}
