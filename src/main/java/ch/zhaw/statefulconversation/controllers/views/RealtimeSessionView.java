package ch.zhaw.statefulconversation.controllers.views;

/**
 * Response-DTO fuer eine neu erstellte Realtime-Session.
 * Enthaelt das Client-Secret zur Authentifizierung, das verwendete Modell
 * und die WebSocket-URL fuer die Echtzeit-Verbindung.
 */
public class RealtimeSessionView {
    private String clientSecret;
    private String model;
    private String realtimeUrl;

    public RealtimeSessionView(String clientSecret, String model, String realtimeUrl) {
        this.clientSecret = clientSecret;
        this.model = model;
        this.realtimeUrl = realtimeUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getModel() {
        return model;
    }

    public String getRealtimeUrl() {
        return realtimeUrl;
    }
}
