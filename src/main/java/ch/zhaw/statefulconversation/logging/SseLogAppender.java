package ch.zhaw.statefulconversation.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Benutzerdefinierter Logback-Appender, der Log-Ereignisse an den {@link LogStreamBroadcaster} weiterleitet.
 *
 * <p>Wird in der Logback-Konfiguration registriert und faengt alle Log-Events ab.
 * Falls der Broadcaster noch nicht initialisiert ist (z.B. beim Anwendungsstart),
 * werden die Events stillschweigend verworfen.
 */
public class SseLogAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent eventObject) {
        LogStreamBroadcaster broadcaster = LogStreamBroadcaster.getInstance();
        if (broadcaster == null) {
            return;
        }
        LogEvent event = new LogEvent(
                eventObject.getTimeStamp(),
                eventObject.getLevel().toString(),
                eventObject.getLoggerName(),
                eventObject.getFormattedMessage());
        broadcaster.publish(event);
    }
}
