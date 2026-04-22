package ch.zhaw.statefulconversation.model.commons.actions;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Utterances;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

/**
 * Transition-Action, die die Konversationshistorie von einem State in einen anderen kopiert.
 *
 * Wird im Oblivio-Biographer verwendet, um Utterances vom Konversations-State (Conv)
 * in den Bestaetigungs-State (Confirm) zu uebertragen, damit der Confirm-State
 * den bisherigen Gespraechsverlauf kennt.
 *
 * @see Action
 * @see Utterances#append(Utterances, State)
 */
@Entity
public class TransferUtterancesAction extends Action {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private State to;

    protected TransferUtterancesAction() {

    }

    public TransferUtterancesAction(State to) {
        super(null);
        this.to = to;
    }

    // Kopiert alle Utterances aus dem Quell-State in den Ziel-State
    @Override
    public void execute(Utterances utterances) {
        this.to.getUtterances().append(utterances, this.to);
    }

    @Override
    public String toString() {
        return "TransferUtterancesAction IS-A " + super.toString();
    }
}
