package ch.zhaw.statefulconversation.spi;

/**
 * Datenklasse fuer die Metadaten einer OpenAI Realtime-Session.
 *
 * <p>Enthaelt das Client Secret (zur Authentifizierung), das verwendete Modell
 * und die WebSocket-URL fuer die Realtime-Verbindung.
 */
public class RealtimeSessionInfo {
    private final String clientSecret;
    private final String model;
    private final String realtimeUrl;

    public RealtimeSessionInfo(String clientSecret, String model, String realtimeUrl) {
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
