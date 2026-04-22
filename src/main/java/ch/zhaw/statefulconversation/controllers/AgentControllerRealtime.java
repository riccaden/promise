package ch.zhaw.statefulconversation.controllers;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.statefulconversation.controllers.views.PromptResponseView;
import ch.zhaw.statefulconversation.controllers.views.UtteranceRequest;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

/**
 * Experimenteller REST-Controller fuer Realtime-/Streaming-Interaktion mit Agents.
 *
 * Im Gegensatz zum normalen {@link AgentController} wird hier die LLM-Antwort
 * nicht serverseitig generiert. Stattdessen liefert {@code /prompt} den aktuellen
 * System-Prompt + Conversation-Verlauf, damit ein externer Client (z.B. WebRTC)
 * die Sprachgenerierung selbst uebernehmen kann. {@code /acknowledge} und
 * {@code /assistant} speichern User- bzw. Assistant-Nachrichten nachtraeglich.
 *
 * Endpoint-Pfade: /{agentID}/prompt, /{agentID}/acknowledge, /{agentID}/assistant
 */
@RestController
public class AgentControllerRealtime {

    @Autowired
    private AgentRepository repository;

    // Liefert den vollstaendigen System-Prompt und Conversation-Verlauf fuer externe LLM-Aufrufe
    @GetMapping("{agentID}/prompt")
    public ResponseEntity<PromptResponseView> prompt(@PathVariable UUID agentID) {
        if (agentID == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        PromptResponseView view = new PromptResponseView(agentMaybe.get().getTotalPrompt(),
                agentMaybe.get().isActive());
        return new ResponseEntity<>(view, HttpStatus.OK);
    }

    // Speichert eine User-Nachricht nachtraeglich im Agent (ohne LLM-Antwort auszuloesen)
    @PostMapping("{agentID}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable UUID agentID, @RequestBody UtteranceRequest userSays) {
        if (agentID == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (userSays == null || userSays.getContent() == null || userSays.getContent().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Agent agent = agentMaybe.get();
        agent.acknowledge(userSays.getContent());
        this.repository.save(agent);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Speichert eine extern generierte Assistant-Antwort im Conversation-Verlauf
    @PostMapping("{agentID}/assistant")
    public ResponseEntity<Void> assistant(@PathVariable UUID agentID, @RequestBody UtteranceRequest assistantSays) {
        if (agentID == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (assistantSays == null || assistantSays.getContent() == null || assistantSays.getContent().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Agent agent = agentMaybe.get();
        agent.appendAssistantResponse(assistantSays.getContent());
        this.repository.save(agent);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
