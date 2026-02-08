package ch.zhaw.statefulconversation.controllers.views;

/**
 * Request body for Text-to-Speech conversion
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
