package ch.zhaw.statefulconversation.model.commons.actions;

import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Utterances;
import ch.zhaw.statefulconversation.spi.LMOpenAI;
import jakarta.persistence.Entity;

/**
 * Transition-Action, die Daten aus der Konversation via LLM extrahiert und im {@link Storage} ablegt.
 *
 * Verwendet {@link ch.zhaw.statefulconversation.spi.LMOpenAI#extract} mit einem statischen
 * (festen) Prompt, um strukturierte Informationen (z.B. JSON) aus dem Gespraechsverlauf
 * zu gewinnen. Das Ergebnis wird unter dem konfigurierten {@code storageKeyTo} gespeichert.
 *
 * Im Oblivio-Biographer wird diese Action genutzt, um Block-Zusammenfassungen
 * (block1–block10) nach jedem Gespraechsabschnitt zu extrahieren.
 *
 * @see Action
 * @see Storage
 */
@Entity
public class StaticExtractionAction extends Action {

    protected StaticExtractionAction() {

    }

    public StaticExtractionAction(String actionPrompt, Storage storage, String storageKeyTo) {
        super(actionPrompt, storage, storageKeyTo);
    }

    // Sendet den Extraktions-Prompt an das LLM und speichert das JSON-Ergebnis im Storage
    @Override
    public void execute(Utterances utterances) {
        JsonElement result = LMOpenAI.extract(utterances, this.getPrompt());
        this.getStorage().put(this.getStorageKeyTo(), result);
    }

    @Override
    public String toString() {
        return "StaticExtractionAction IS-A " + super.toString();
    }
}
