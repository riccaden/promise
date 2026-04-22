package ch.zhaw.statefulconversation.controllers.dto;

/**
 * Request-DTO fuer die Erstellung eines Biographer-Agents.
 *
 * Enthaelt nur minimale Konfiguration (Name, Beschreibung, Sprache, Nickname),
 * da die 20-State-Prompt-Kette in {@link AgentMetaUtility#createBiographerAgent}
 * vordefiniert ist. Das Feld {@code language} steuert die Gespraechssprache
 * (de, en, fr, it, tr, ko, ja, zh), {@code nickname} wird optional fuer die
 * persoenliche Ansprache im Gespraech verwendet.
 *
 * Endpoint: POST /agent/biographer
 */
public class BiographerAgentCreateDTO {
    private int type;
    private String userId;
    private String agentName;
    private String agentDescription;
    private String language;
    private String nickname;

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

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
