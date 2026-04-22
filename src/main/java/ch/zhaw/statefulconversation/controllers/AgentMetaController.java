package ch.zhaw.statefulconversation.controllers;

import java.util.ArrayList;
import java.util.List;
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

import ch.zhaw.statefulconversation.controllers.dto.SingleStateAgentCreateDTO;
import ch.zhaw.statefulconversation.controllers.dto.BiographerAgentCreateDTO;
import ch.zhaw.statefulconversation.controllers.views.AgentInfoView;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

/**
 * REST-Controller fuer die Erstellung und Verwaltung von Agents.
 *
 * Bietet Endpoints zum Auflisten aller Agents sowie zum Erstellen
 * neuer Agents (Legacy-Einzelzustand oder Biographer mit 20-State-Kette).
 * Die eigentliche Agent-Konstruktion wird an {@link AgentMetaUtility} delegiert.
 *
 * Basis-Pfad: /agent/...
 */
@RestController
public class AgentMetaController {

    @Autowired
    private AgentRepository repository;

    // Listet alle gespeicherten Agents mit ihren Metadaten auf
    @GetMapping("agent")
    public ResponseEntity<List<AgentInfoView>> findAll() {
        List<Agent> agents = this.repository.findAll();
        List<AgentInfoView> result = new ArrayList<AgentInfoView>();
        for (Agent current : agents) {
            result.add(new AgentInfoView(current.getId(), current.getName(), current.getDescription(),
                    current.isActive()));
        }
        return new ResponseEntity<List<AgentInfoView>>(result, HttpStatus.OK);
    }

    // Gibt alle Agents inkl. vollstaendiger Conversation-Daten zurueck (Debug-Endpoint)
    @GetMapping("agent/conversation")
    public ResponseEntity<List<Agent>> findAllConversation() {
        List<Agent> agents = this.repository.findAll();
        return new ResponseEntity<List<Agent>>(agents, HttpStatus.OK);
    }

    @GetMapping("agent/{id}")
    public ResponseEntity<AgentInfoView> findById(
            @PathVariable(required = true) @org.springframework.lang.NonNull UUID id) {
        Optional<Agent> agentMaybe = this.repository.findById(id);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<AgentInfoView>(
                new AgentInfoView(agentMaybe.get().getId(), agentMaybe.get().getName(),
                        agentMaybe.get().getDescription(), agentMaybe.get().isActive()),
                HttpStatus.OK);
    }

    @GetMapping("agent/{id}/conversation")
    public ResponseEntity<Agent> findByIdConversation(
            @PathVariable(required = true) @org.springframework.lang.NonNull UUID id) {
        Optional<Agent> agentMaybe = this.repository.findById(id);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<Agent>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Agent>(agentMaybe.get(), HttpStatus.OK);
    }

    // Erstellt einen Legacy-Agent mit einem einzelnen State und einer Transition zu Final
    @PostMapping("agent/singlestate")
    public ResponseEntity<AgentInfoView> create(@RequestBody SingleStateAgentCreateDTO data) {
        if (data == null) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.BAD_REQUEST);
        }
        Agent agent;
        if (AgentMetaType.singleState.getValue() == data.getType()) {
            agent = AgentMetaUtility.createSingleStateAgent(data);
        } else { // have as many 'else if (...getValue() == data.getType())' as needed here
            System.err.println("unknown agent type " + data.getType());
            return new ResponseEntity<AgentInfoView>(HttpStatus.BAD_REQUEST);
        }

        if (agent == null) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.BAD_REQUEST);
        }
        this.repository.save(agent);

        var result = new AgentInfoView(agent.getId(), agent.getName(), agent.getDescription(), agent.isActive());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Erstellt einen Biographer-Agent mit 20-State-Kette (10 Bloecke x 2 States: Conv + Confirm)
    @PostMapping("agent/biographer")
    public ResponseEntity<AgentInfoView> createBiographer(@RequestBody BiographerAgentCreateDTO data) {
        if (data == null) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.BAD_REQUEST);
        }

        if (AgentMetaType.biographer.getValue() != data.getType()) {
            System.err.println("invalid agent type for biographer endpoint: " + data.getType());
            return new ResponseEntity<AgentInfoView>(HttpStatus.BAD_REQUEST);
        }

        Agent agent = AgentMetaUtility.createBiographerAgent(data);

        if (agent == null) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.BAD_REQUEST);
        }

        this.repository.save(agent);

        var result = new AgentInfoView(agent.getId(), agent.getName(), agent.getDescription(), agent.isActive());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
