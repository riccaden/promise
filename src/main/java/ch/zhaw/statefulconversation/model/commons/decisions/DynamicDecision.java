package ch.zhaw.statefulconversation.model.commons.decisions;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

/**
 * Dynamische Transition-Entscheidung, deren Prompt zur Laufzeit aus {@link Storage}-Werten
 * zusammengesetzt wird.
 *
 * Der Prompt-Template enthaelt Platzhalter (z.B. {@code ${topics}}), die via
 * {@link ch.zhaw.statefulconversation.utils.NamedParametersFormatter} mit aktuellen
 * Werten aus dem Storage ersetzt werden. Erwartet, dass der Storage-Wert ein JsonArray ist.
 *
 * @see StaticDecision
 * @see Decision
 */
@Entity
public class DynamicDecision extends Decision {

    protected DynamicDecision() {

    }

    public DynamicDecision(String decisionPromptTemplate, Storage storage, String storageKeyFrom) {
        super(decisionPromptTemplate, storage, storageKeyFrom);
    }

    @Override
    protected String getPrompt() {
        Map<String, JsonElement> valuesForKeys = this.getValuesForKeys();

        if (!(valuesForKeys.values().iterator().next() instanceof JsonArray)) {
            throw new RuntimeException(
                    "expected storageKeyFrom " + this.getStorageKeysFrom()
                            + " being associated to a list (JsonArray) but enountered "
                            + valuesForKeys.values().iterator().next().getClass()
                            + valuesForKeys.values().iterator().next()
                            + " instead");
        }

        return NamedParametersFormatter.format(super.getPrompt(), valuesForKeys);
    }

    @Override
    public String toString() {
        return "DynamicDecision IS-A " + super.toString();
    }
}