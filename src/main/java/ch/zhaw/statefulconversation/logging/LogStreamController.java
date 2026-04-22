package ch.zhaw.statefulconversation.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST-Controller fuer das Echtzeit-Log-Streaming via Server-Sent Events.
 *
 * <p>Stellt den Endpoint {@code GET /logs/stream} bereit, ueber den Clients
 * einen SSE-Stream aller Applikations-Logs abonnieren koennen.
 */
@RestController
@RequestMapping("logs")
public class LogStreamController {

    @Autowired
    private LogStreamBroadcaster broadcaster;

    @GetMapping(path = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return this.broadcaster.subscribe();
    }
}
