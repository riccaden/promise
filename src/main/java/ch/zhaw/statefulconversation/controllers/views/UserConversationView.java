package ch.zhaw.statefulconversation.controllers.views;

import java.time.Instant;
import java.util.UUID;

/**
 * View-Klasse für User-Conversations
 */
public class UserConversationView {
    private UUID agentId;
    private String agentName;
    private String role;
    private String content;
    private String stateName;
    private Instant timestamp;

    public UserConversationView(UUID agentId, String agentName, String role, String content, String stateName,
            Instant timestamp) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.role = role;
        this.content = content;
        this.stateName = stateName;
        this.timestamp = timestamp;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getStateName() {
        return stateName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
