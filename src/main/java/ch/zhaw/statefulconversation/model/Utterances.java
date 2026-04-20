package ch.zhaw.statefulconversation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.zhaw.statefulconversation.spi.LMOpenAI;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
public class Utterances {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utterances.class);
    private static final String ASSISTANT = "assistant";
    private static final String USER = "user";
    private static final String SYSTEM = "system";
    private static final int USER_MESSAGE_COMPACT_THRESHOLD = 20;
    private static final int MESSAGES_TO_KEEP = 10;

    @Id
    @GeneratedValue
    private UUID id;

    public UUID getID() {
        return this.id;
    }

    @OneToMany(mappedBy = "utterances", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("createdDate ASC")
    private List<Utterance> utteranceList;

    public Utterances() {
        this.utteranceList = new ArrayList<Utterance>();
    }

    public void append(Utterances source, State state) {
        for (Utterance current : source.toList()) {
            Utterance utterance = new Utterance(current.getRole(), current.getContent(), state.getName());
            utterance.setUtterances(this);
            this.utteranceList.add(utterance);
        }
    }

    public void appendAssistantSays(String assistantSays, State state) {
        Utterance utterance = new Utterance(Utterances.ASSISTANT, assistantSays, state.getName());
        utterance.setUtterances(this);
        this.utteranceList.add(utterance);
    }

    public void appendUserSays(String userSays, State state) {
        Utterance utterance = new Utterance(Utterances.USER, userSays, state.getName());
        utterance.setUtterances(this);
        this.utteranceList.add(utterance);
    }

    /*
     * This one is assuming the last utterance is from the user
     * (used in RemoveLastUtteranceAction)
     */
    public void removeLastUtterance() {
        if (!this.utteranceList.getLast().getRole().equals(Utterances.USER)) {
            throw new RuntimeException("assumption that last utterance has role == user failed");
        }
        this.utteranceList.removeLast();
    }

    public String removeLastTwoUtterances() {
        if (!this.utteranceList.getLast().getRole().equals(Utterances.ASSISTANT)) {
            throw new RuntimeException("assumption that last utterance has role == assistant failed");
        }
        this.utteranceList.removeLast();

        // the following loop is to accomodate the possibility that the assistant had
        // multiple responses in a row (cf. HTML reponses)
        while (this.utteranceList.getLast().getRole().equals(Utterances.ASSISTANT)) {
            this.utteranceList.removeLast();
        }

        if (!this.utteranceList.getLast().getRole().equals(Utterances.USER)) {
            throw new RuntimeException(
                    "assumption that when removing all assistant utterances only user utterance remains failed");
        }

        Utterance lastUserUtterance = this.utteranceList.getLast();
        return lastUserUtterance.getContent();
    }

    public boolean isEmpty() {
        return this.utteranceList.isEmpty();
    }

    public void reset() {
        this.utteranceList.clear();
    }

    public List<Utterance> toList() {
        List<Utterance> result = List.copyOf(this.utteranceList);
        return result;
    }

    /**
     * Compacts conversation history when user messages exceed the threshold.
     * Summarises the oldest messages into a single system message, keeping
     * the most recent ones intact. This mirrors natural human memory —
     * older details fade into a general impression while recent exchanges
     * stay vivid.
     */
    public void compactIfNeeded() {
        long userMessageCount = this.utteranceList.stream()
                .filter(u -> USER.equals(u.getRole()))
                .count();

        if (userMessageCount <= USER_MESSAGE_COMPACT_THRESHOLD) {
            return;
        }

        // Check if already compacted (first message is a system summary)
        if (!this.utteranceList.isEmpty()
                && SYSTEM.equals(this.utteranceList.get(0).getRole())
                && this.utteranceList.get(0).getContent().startsWith("[Zusammenfassung")) {
            return;
        }

        LOGGER.info("Compacting conversation: {} user messages exceed threshold of {}",
                userMessageCount, USER_MESSAGE_COMPACT_THRESHOLD);

        int totalSize = this.utteranceList.size();
        int splitPoint = totalSize - MESSAGES_TO_KEEP;
        if (splitPoint <= 0) {
            return;
        }

        // Build text of older messages for summarisation
        List<Utterance> olderMessages = new ArrayList<>(this.utteranceList.subList(0, splitPoint));
        String conversationText = olderMessages.stream()
                .map(u -> u.getRole() + ": " + u.getContent())
                .collect(Collectors.joining("\n"));

        // Ask LLM to summarise
        String summaryPrompt = "Fasse das folgende Gespräch in 3-5 Sätzen zusammen. "
                + "Behalte die wichtigsten Themen, gestellten Fragen und emotionalen Momente. "
                + "Schreibe die Zusammenfassung in der Sprache des Gesprächs.";
        String summary;
        try {
            Utterances tempUtterances = new Utterances();
            Utterance tempMsg = new Utterance(USER, conversationText, null);
            tempMsg.setUtterances(tempUtterances);
            tempUtterances.utteranceList.add(tempMsg);
            summary = LMOpenAI.summariseOffline(tempUtterances, summaryPrompt);
        } catch (Exception e) {
            LOGGER.warn("Failed to summarise conversation, skipping compaction: {}", e.getMessage());
            return;
        }

        // Remove older messages
        for (Utterance old : olderMessages) {
            old.setUtterances(null);
        }
        this.utteranceList.subList(0, splitPoint).clear();

        // Insert summary as first message
        Utterance summaryUtterance = new Utterance(SYSTEM,
                "[Zusammenfassung des bisherigen Gesprächs]\n" + summary, null);
        summaryUtterance.setUtterances(this);
        this.utteranceList.add(0, summaryUtterance);

        LOGGER.info("Compacted: removed {} old messages, kept {}, added summary",
                olderMessages.size(), MESSAGES_TO_KEEP);
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("");
        for (Utterance current : this.utteranceList) {
            result.append(current.getRole() + ": " + current.getContent() + "\n");
        }
        return result.toString();
    }
}
