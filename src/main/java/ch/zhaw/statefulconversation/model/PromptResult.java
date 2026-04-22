package ch.zhaw.statefulconversation.model;

import java.util.List;

/**
 * Buendelt einen {@link State}, dessen zusammengesetzten System-Prompt und die
 * aktuelle Konversationshistorie.
 *
 * Wird von {@link State#getPromptBundle()} zurueckgegeben und dient der Diagnose
 * bzw. dem Debugging, um den vollstaendigen Prompt-Kontext einzusehen, der an das
 * LLM gesendet wird.
 *
 * @see State#getPromptBundle()
 */
public class PromptResult {
    private final String stateName;
    private final String systemPrompt;
    private final List<Utterance> conversation;
    private final boolean starting;

    public PromptResult(State state, String systemPrompt, List<Utterance> conversation) {
        this.stateName = state.getName();
        this.systemPrompt = systemPrompt;
        this.conversation = conversation;
        this.starting = state.isStarting();
    }

    public String getStateName() {
        return stateName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public List<Utterance> getConversation() {
        return conversation;
    }

    public boolean isStarting() {
        return starting;
    }
}
