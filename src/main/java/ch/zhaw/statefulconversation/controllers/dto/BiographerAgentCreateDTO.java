package ch.zhaw.statefulconversation.controllers.dto;

/**
 * DTO for creating Biographer agents with minimal configuration.
 * Biographer agents use predefined prompts for gathering life stories.
 */
public class BiographerAgentCreateDTO {
    private int type;
    private String userId;
    private String agentName;
    private String agentDescription;
    private String language;

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAgentName() {
        return this.agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentDescription() {
        return this.agentDescription;
    }

    public void setAgentDescription(String agentDescription) {
        this.agentDescription = agentDescription;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
