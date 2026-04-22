package ch.zhaw.statefulconversation.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.statefulconversation.controllers.views.RealtimeSessionView;
import ch.zhaw.statefulconversation.spi.RealtimeSessionClient;
import ch.zhaw.statefulconversation.spi.RealtimeSessionInfo;

/**
 * REST-Controller fuer die Erstellung von Realtime-Sessions.
 *
 * Erstellt ueber den {@link RealtimeSessionClient} eine neue Session
 * (z.B. fuer WebRTC-basierte Sprachinteraktion) und gibt die
 * Verbindungsdaten (Client-Secret, Modell, URL) zurueck.
 *
 * Endpoint: POST /realtime/session
 */
@RestController
public class RealtimeController {

    @PostMapping("realtime/session")
    public ResponseEntity<RealtimeSessionView> createSession() {
        RealtimeSessionInfo session = RealtimeSessionClient.createSession();
        RealtimeSessionView view = new RealtimeSessionView(
                session.getClientSecret(),
                session.getModel(),
                session.getRealtimeUrl());
        return new ResponseEntity<>(view, HttpStatus.OK);
    }
}
