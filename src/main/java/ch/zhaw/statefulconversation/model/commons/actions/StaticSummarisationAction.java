package ch.zhaw.statefulconversation.model.commons.actions;

import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Utterances;
import ch.zhaw.statefulconversation.spi.LMOpenAI;
import jakarta.persistence.Entity;

/**
 * Transition-Action, die die Konversation via LLM zusammenfasst und das Ergebnis im {@link Storage} ablegt.
 *
 * Aehnlich wie {@link StaticExtractionAction}, verwendet aber
 * {@link ch.zhaw.statefulconversation.spi.LMOpenAI#summarise} statt extract.
 * Der statische Prompt definiert, wie die Zusammenfassung erstellt werden soll.
 *
 * @see Action
 * @see StaticExtractionAction
 */
@Entity
public class StaticSummarisationAction extends Action {

    protected StaticSummarisationAction() {

    }

    public StaticSummarisationAction(String actionPrompt, Storage storage, String storageKeyTo) {
        super(actionPrompt, storage, storageKeyTo);
    }

    @Override
    public void execute(Utterances utterances) {
        JsonElement result = LMOpenAI.summarise(utterances, this.getPrompt());
        this.getStorage().put(this.getStorageKeyTo(), result);
    }

    @Override
    public String toString() {
        return "StaticSummarisationAction IS-A " + super.toString();
    }
}
