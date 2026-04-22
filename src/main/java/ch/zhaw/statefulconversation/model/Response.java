package ch.zhaw.statefulconversation.model;

/**
 * Kapselung einer Assistenz-Antwort zusammen mit dem Namen des erzeugenden {@link State}.
 *
 * Wird von {@link State#start()} und {@link State#respond(String)} zurueckgegeben,
 * um sowohl den Antworttext als auch den Kontext (welcher State die Antwort generiert hat)
 * an den Aufrufer weiterzugeben.
 *
 * @see State
 * @see Agent
 */
public class Response {

    private String stateName;
    private String text;

    public Response(State state, String text) {
        this.stateName = state.getName();
        this.text = text;
    }

    public Response(State state) {
        this(state, null);
    }

    public String getStateName() {
        return stateName;
    }

    public String getText() {
        return text;
    }
}
