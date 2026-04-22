package ch.zhaw.statefulconversation.model.commons.actions;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Utterances;
import jakarta.persistence.Entity;

/**
 * Transition-Action, die ein Thema aus einer dynamischen Liste im {@link Storage} entfernt.
 *
 * Liest die Themenliste ({@code storageKeyFrom}, JsonArray) und das ausgewaehlte Thema
 * ({@code storageKeyTo}, JsonPrimitive oder JsonObject) aus dem Storage, entfernt das
 * ausgewaehlte Thema aus der Liste und schreibt die aktualisierte Liste zurueck.
 *
 * Wird typischerweise zusammen mit {@link DynamicSingleChoiceStateShrinking} verwendet,
 * um bereits bearbeitete Themen aus der Auswahlliste zu streichen.
 *
 * @see Action
 * @see DynamicSingleChoiceStateShrinking
 */
@Entity
public class DynamicRemoveTopicAction extends Action {

    protected DynamicRemoveTopicAction() {

    }

    public DynamicRemoveTopicAction(String actionPromptTemplate, Storage storage, String storageKeyFrom,
            String storageKeyTo) {
        super(actionPromptTemplate, storage, storageKeyFrom, storageKeyTo);
    }

    // Liest Themenliste und ausgewaehltes Thema aus Storage, entfernt das Thema
    // und schreibt die reduzierte Liste zurueck. Unterstuetzt JsonPrimitive und JsonObject.
    @Override
    public void execute(Utterances utterances) {
        // Themenliste aus Storage laden (storageKeyFrom)
        JsonElement topicsTo = this.getStorage().get(this.getStorageKeysFrom().get(0));
        // Ausgewaehltes/zu entfernendes Thema laden (storageKeyTo)
        JsonElement topicFrom = this.getStorage().get(this.getStorageKeyTo());

        if (!(topicsTo instanceof JsonArray)) {
            throw new RuntimeException(
                    "Invalid data in storage. Expected value for key " + this.getStorageKeysFrom().get(0)
                            + " to be instance of JsonArray but was " + topicsTo.getClass() + " instead");
        }

        String stringToRemove = null;
        if (topicFrom instanceof JsonPrimitive) {
            stringToRemove = topicFrom.getAsJsonPrimitive().getAsString();
        } else if (topicFrom instanceof JsonObject) {
            JsonObject temporaryObject = topicFrom.getAsJsonObject();
            if (temporaryObject.entrySet().size() != 1) {
                throw new RuntimeException(
                        "Invalid data in storage. Expected JsonObject with only one key value pair but found "
                                + temporaryObject.entrySet().size() + " pairs instead");
            }
            JsonElement temporaryElement = temporaryObject.entrySet().iterator().next().getValue();
            if (!(temporaryElement instanceof JsonPrimitive)) {
                throw new RuntimeException(
                        "Invalid data in storage. Expected JsonObject to have a value that is an instance of JsonPrimitive but found "
                                + temporaryElement.getClass() + " instead");
            }
            stringToRemove = temporaryElement.getAsJsonPrimitive().getAsString();
        } else {
            throw new RuntimeException(
                    "Invalid data in storage. Expected value for key " + this.getStorageKeyTo()
                            + " to be instance of JsonPrimitive or JsonObject but was " + topicFrom.getClass()
                            + " instead");
        }
        List<String> topicsList = Storage.toListOfString(topicsTo);
        if (!topicsList.contains(stringToRemove)) {
            throw new RuntimeException(
                    "List " + topicsList + " does not contain the item " + stringToRemove + " to be removed");
        }
        topicsList.remove(stringToRemove);
        this.getStorage().put(this.getStorageKeysFrom().get(0), Storage.toJsonElement(topicsList));
    }

    @Override
    public String toString() {
        return "DynamicRemoveTopicAction IS-A " + super.toString();
    }
}
