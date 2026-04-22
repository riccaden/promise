package ch.zhaw.statefulconversation.model;

import java.util.List;

import jakarta.persistence.Entity;

/**
 * Terminaler Zustand, der das Ende einer Konversation markiert.
 *
 * Ein Final-State ist inaktiv ({@link #isActive()} gibt false zurueck), wodurch der
 * uebergeordnete {@link Agent} erkennt, dass die Konversation abgeschlossen ist.
 * Er enthaelt keine Transitions und verwendet einen Standard-Abschiedsprompt.
 *
 * Im Oblivio-Biographer bildet Final den letzten Zustand der 20-State-Kette;
 * in Legacy-Agenten dient er als Exit-State. Verschiedene Konstruktoren ermoeglichen
 * individuelle Prompts und Starter-Prompts (z.B. fuer mehrsprachige Verabschiedungen).
 *
 * @see State
 * @see Agent#isActive()
 */
@Entity
public class Final extends State {

    protected Final() {

    }

    private static final String FINAL_PROMPT = """
            This is the final state and the conversation is complete.
            If the user sends further messages, do not restart or continue the interaction, ask no questions, and introduce no new topics.
            Briefly acknowledge the message, state that the conversation has ended, and note that a new session is required to continue.
            Keep responses short and warm.
            """;;
    private static final String FINAL_STARTER_PROMPT = "Give a very brief, courteous goodbye to end on a positive and respectful note.";

    public Final(String name) {
        super(Final.FINAL_PROMPT, name, Final.FINAL_STARTER_PROMPT, List.of());
    }

    public Final(String name, boolean isStarting, String summarisePrompt) {
        super(Final.FINAL_PROMPT, name, Final.FINAL_STARTER_PROMPT, List.of(), summarisePrompt, isStarting, false);
    }

    public Final(String name, String prompt) {
        super(prompt, name, Final.FINAL_STARTER_PROMPT, List.of());
    }

    public Final(String name, String prompt, boolean isStarting, String summarisePrompt) {
        super(prompt, name, Final.FINAL_STARTER_PROMPT, List.of(), summarisePrompt, isStarting, false);
    }

    public Final(String name, String prompt, String starterPrompt) {
        super(prompt, name, starterPrompt, List.of());
    }

    public Final(String name, String prompt, String starterPrompt, boolean isStarting, String summarisePrompt) {
        super(prompt, name, starterPrompt, List.of(), summarisePrompt, isStarting, false);
    }

    // Final-State ist per Definition inaktiv — signalisiert dem Agenten das Konversationsende
    @Override
    public boolean isActive() {
        return false;
    }

    // @TODO for diagnostic purpose only, remove
    public String summarise() {
        throw new RuntimeException("we want to avoid summarise being invoked on final states");
    }

    @Override
    public String toString() {
        return "Final IS-A " + super.toString();
    }
}
