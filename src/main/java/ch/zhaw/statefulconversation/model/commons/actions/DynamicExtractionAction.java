package ch.zhaw.statefulconversation.model.commons.actions;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Utterances;
import ch.zhaw.statefulconversation.spi.LMOpenAI;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

/**
 * Dynamische Variante der {@link StaticExtractionAction} — der Extraktions-Prompt wird
 * zur Laufzeit aus {@link Storage}-Werten zusammengesetzt.
 *
 * Der Prompt-Template enthaelt Platzhalter (z.B. {@code ${topics}}), die ueber
 * {@link ch.zhaw.statefulconversation.utils.NamedParametersFormatter} mit aktuellen
 * Werten aus dem Storage ersetzt werden. Erwartet, dass der Storage-Wert ein
 * {@link com.google.gson.JsonArray} ist.
 *
 * @see StaticExtractionAction
 * @see Action
 */
@Entity
public class DynamicExtractionAction extends Action {

    protected DynamicExtractionAction() {

    }

    public DynamicExtractionAction(String actionPromptTemplate, Storage storage, String storageKeyFrom,
            String storageKeyTo) {
        super(actionPromptTemplate, storage, storageKeyFrom, storageKeyTo);
    }

    // Ersetzt Platzhalter im Prompt-Template mit aktuellen Storage-Werten (JsonArray erwartet)
    @Override
    protected String getPrompt() {
        Map<String, JsonElement> valuesForKeys = this.getValuesForKeys();
        if (!(valuesForKeys.values().iterator().next() instanceof JsonArray)) {
            throw new RuntimeException(
                    "expected storageKeyFrom being associated to a list (JsonArray) but enountered "
                            + valuesForKeys.values().iterator().next().getClass()
                            + valuesForKeys.values().iterator().next()
                            + " instead");
        }

        return NamedParametersFormatter.format(super.getPrompt(), valuesForKeys);
    }

    @Override
    public void execute(Utterances utterances) {
        JsonElement result = LMOpenAI.extract(utterances, this.getPrompt());
        this.getStorage().put(this.getStorageKeyTo(), result);
    }

    @Override
    public String toString() {
        return "DynamicExtractionAction IS-A " + super.toString();
    }
}
