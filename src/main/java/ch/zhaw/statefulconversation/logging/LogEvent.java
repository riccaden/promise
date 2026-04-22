package ch.zhaw.statefulconversation.logging;

/**
 * Datenklasse fuer ein einzelnes Log-Ereignis im SSE-Log-Streaming.
 *
 * <p>Enthaelt Zeitstempel, Log-Level, Logger-Name und die formatierte Nachricht.
 * Instanzen werden vom {@link SseLogAppender} erzeugt und ueber den
 * {@link LogStreamBroadcaster} an verbundene Clients gesendet.
 */
public class LogEvent {
    private final long timestamp;
    private final String level;
    private final String logger;
    private final String message;

    public LogEvent(long timestamp, String level, String logger, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.logger = logger;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getLogger() {
        return logger;
    }

    public String getMessage() {
        return message;
    }
}
