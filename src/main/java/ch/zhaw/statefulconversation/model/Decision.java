package ch.zhaw.statefulconversation.model;

import java.util.List;

import jakarta.persistence.Entity;

/**
 * Abstrakte Basisklasse fuer Transition-Entscheidungen (Guards/Trigger) im PROMISE Framework.
 *
 * Eine Decision enthaelt einen Prompt, der als Ja/Nein-Frage an das LLM gesendet wird
 * (via {@link ch.zhaw.statefulconversation.spi.LMOpenAI#decide}). Gibt das LLM "true"
 * zurueck, gilt die Bedingung als erfuellt und die zugehoerige {@link Transition} kann feuern.
 *
 * Erbt von {@link Prompt} — statische Decisions verwenden einen festen Prompt-Text,
 * dynamische Varianten (z.B. {@link DynamicDecision}) setzen Werte aus dem {@link Storage}
 * zur Laufzeit in den Prompt ein.
 *
 * @see StaticDecision
 * @see DynamicDecision
 * @see Transition#decide(Utterances)
 */
@Entity
public abstract class Decision extends Prompt {

    protected Decision() {

    }

    public Decision(String decisionPrompt) {
        super(decisionPrompt);
    }

    public Decision(String decisionPrompt, Storage storage, String storageKeysFrom) {
        super(decisionPrompt, storage, List.of(storageKeysFrom));
    }

    @Override
    public String toString() {
        return "Decision IS-A " + super.toString();
    }
}
