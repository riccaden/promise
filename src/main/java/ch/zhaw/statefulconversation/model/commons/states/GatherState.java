package ch.zhaw.statefulconversation.model.commons.states;

import java.util.List;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import jakarta.persistence.Entity;

/**
 * Vorkonfigurierter State, der Informationen vom Benutzer einsammelt (Slot-Filling).
 *
 * Fragt den Benutzer nach Werten fuer eine definierte Liste von Slots. Sobald das LLM
 * erkennt, dass alle Slots befuellt sind ({@link StaticDecision}), werden die Werte
 * via {@link StaticExtractionAction} extrahiert und im {@link Storage} abgelegt.
 * Danach erfolgt die Transition zum Folgezustand.
 *
 * Die Slot-Namen werden direkt in die Prompts fuer Decision und Action eingebaut.
 *
 * @see State
 * @see DynamicGatherState
 */
@Entity
public class GatherState extends State {

        private static final String GATHER_PROMPT = "Ask the user to provide one value for each of the following slots: ";
        private static final String GATHER_STARTER_PROMPT = "Ask the user.";
        private static final String GATHER_TRIGGER = "Examine the following chat and decide if the user provides all values for the following slots: ";
        private static final String GATHER_ACTION = "Examine the following chat and extract each value for all of the following slots: ";

        protected GatherState() {

        }

        public GatherState(String name, List<String> slots, State subsequentState, Storage storage,
                        String storageKeyTo) {
                this(name, slots, subsequentState, storage, storageKeyTo, true, false);
        }

        public GatherState(String name, List<String> slots, State subsequentState, Storage storage, String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(new StringBuilder(GatherState.GATHER_PROMPT)
                                .append(String.join(", ", slots))
                                .toString(),
                                name,
                                GatherState.GATHER_STARTER_PROMPT,
                                List.of(),
                                State.SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of());
                Decision trigger = new StaticDecision(
                                new StringBuilder(GatherState.GATHER_TRIGGER)
                                                .append(String.join(", ", slots))
                                                .toString());
                Action action = new StaticExtractionAction(
                                new StringBuilder(GatherState.GATHER_ACTION)
                                                .append(String.join(", ", slots))
                                                .toString(),
                                storage,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger), List.of(action), subsequentState);
                this.addTransition(transition);
        }

        @Override
        public String toString() {
                return "GatherState IS-A " + super.toString();
        }
}
