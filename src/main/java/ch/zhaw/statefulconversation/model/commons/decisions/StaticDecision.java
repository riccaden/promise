package ch.zhaw.statefulconversation.model.commons.decisions;

import ch.zhaw.statefulconversation.model.Decision;
import jakarta.persistence.Entity;

/**
 * Statische Transition-Entscheidung mit festem Prompt-Text.
 *
 * Der Prompt wird als Ja/Nein-Frage an das LLM gesendet, um zu entscheiden,
 * ob eine {@link ch.zhaw.statefulconversation.model.Transition} feuern soll.
 * Beispiel: "Hat der Benutzer alle geforderten Informationen bereitgestellt?"
 *
 * @see Decision
 * @see DynamicDecision
 */
@Entity
public class StaticDecision extends Decision {

    protected StaticDecision() {

    }

    public StaticDecision(String decisionPrompt) {
        super(decisionPrompt);
    }

    @Override
    public String toString() {
        return "StaticDecision IS-A " + super.toString();
    }
}
