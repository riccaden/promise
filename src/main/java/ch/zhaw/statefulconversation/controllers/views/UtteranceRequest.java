package ch.zhaw.statefulconversation.controllers.views;

/**
 * Request-DTO fuer den /respond-Endpoint. Enthaelt die Textnachricht des Benutzers.
 */
public class UtteranceRequest {
    private String content;

    public UtteranceRequest() {
    }

    public UtteranceRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
