package ch.zhaw.statefulconversation.controllers.views;

import java.util.UUID;

/**
 * View-Klasse für User-Agent-Übersicht
 */
public class UserAgentView {
    private UUID id;
    private String name;
    private String description;
    private boolean active;
    private int conversationCount;

    public UserAgentView(UUID id, String name, String description, boolean active, int conversationCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.conversationCount = conversationCount;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public int getConversationCount() {
        return conversationCount;
    }
}
