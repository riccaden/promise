package ch.zhaw.statefulconversation.controllers.views;

/**
 * Request-DTO fuer den TTS-Endpoint. Enthaelt den Text, der in Sprache umgewandelt werden soll.
 */
public class TTSRequest {
    private String text;

    public TTSRequest() {
    }

    public TTSRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
