package ch.zhaw.statefulconversation.controllers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.statefulconversation.controllers.views.UserConversationView;
import ch.zhaw.statefulconversation.controllers.views.UserAgentView;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Utterance;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

/**
 * Controller für user-spezifische Log-Abfragen
 * Ermöglicht das Tracking und Abrufen von Conversations pro User
 */
@RestController
@RequestMapping("/user")
public class UserLogController {

    @Autowired
    private AgentRepository agentRepository;

    /**
     * Gibt alle Agents eines bestimmten Users zurück
     *
     * @param userId Die User-ID
     * @return Liste von Agent-Informationen
     */
    @GetMapping("/{userId}/agents")
    public ResponseEntity<List<UserAgentView>> getUserAgents(@PathVariable @NonNull String userId) {
        List<Agent> userAgents = agentRepository.findAll().stream()
                .filter(agent -> userId.equals(agent.getUserId()))
                .collect(Collectors.toList());

        List<UserAgentView> result = userAgents.stream()
                .map(agent -> new UserAgentView(
                        agent.getId(),
                        agent.getName(),
                        agent.getDescription(),
                        agent.isActive(),
                        agent.getConversation().size()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Gibt alle Conversations eines Users über alle Agents zurück
     *
     * @param userId Die User-ID
     * @param since  Optional: Nur Conversations seit diesem Zeitpunkt
     * @return Liste von Conversation-Logs
     */
    @GetMapping("/{userId}/conversations")
    public ResponseEntity<List<UserConversationView>> getUserConversations(
            @PathVariable @NonNull String userId,
            @RequestParam(required = false) Long since) {

        List<Agent> userAgents = agentRepository.findAll().stream()
                .filter(agent -> userId.equals(agent.getUserId()))
                .collect(Collectors.toList());

        List<UserConversationView> conversations = userAgents.stream()
                .flatMap(agent -> {
                    List<Utterance> utterances = agent.getConversation();

                    // Filter nach Zeitpunkt falls 'since' angegeben
                    if (since != null) {
                        Instant sinceInstant = Instant.ofEpochMilli(since);
                        utterances = utterances.stream()
                                .filter(u -> u.getCreatedDate().isAfter(sinceInstant))
                                .collect(Collectors.toList());
                    }

                    return utterances.stream()
                            .map(utterance -> new UserConversationView(
                                    agent.getId(),
                                    agent.getName(),
                                    utterance.getRole(),
                                    utterance.getContent(),
                                    utterance.getStateName(),
                                    utterance.getCreatedDate()));
                })
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }

    /**
     * Gibt die Conversation eines spezifischen Agents für einen User zurück
     *
     * @param userId  Die User-ID
     * @param agentId Die Agent-ID
     * @return Liste von Utterances
     */
    @GetMapping("/{userId}/agent/{agentId}/conversation")
    public ResponseEntity<List<UserConversationView>> getUserAgentConversation(
            @PathVariable @NonNull String userId,
            @PathVariable @NonNull UUID agentId) {

        Agent agent = agentRepository.findById(agentId).orElse(null);

        if (agent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Sicherheitscheck: Prüfe ob Agent dem User gehört
        if (!userId.equals(agent.getUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<UserConversationView> conversation = agent.getConversation().stream()
                .map(utterance -> new UserConversationView(
                        agent.getId(),
                        agent.getName(),
                        utterance.getRole(),
                        utterance.getContent(),
                        utterance.getStateName(),
                        utterance.getCreatedDate()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(conversation, HttpStatus.OK);
    }

    /**
     * Gibt Statistiken für einen User zurück
     *
     * @param userId Die User-ID
     * @return Statistik-Informationen
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsView> getUserStats(@PathVariable @NonNull String userId) {
        List<Agent> userAgents = agentRepository.findAll().stream()
                .filter(agent -> userId.equals(agent.getUserId()))
                .collect(Collectors.toList());

        int totalAgents = userAgents.size();
        int activeAgents = (int) userAgents.stream().filter(Agent::isActive).count();
        int totalConversations = userAgents.stream()
                .mapToInt(agent -> agent.getConversation().size())
                .sum();

        UserStatsView stats = new UserStatsView(userId, totalAgents, activeAgents, totalConversations);

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * View-Klasse für User-Statistiken
     */
    public static class UserStatsView {
        private String userId;
        private int totalAgents;
        private int activeAgents;
        private int totalConversations;

        public UserStatsView(String userId, int totalAgents, int activeAgents, int totalConversations) {
            this.userId = userId;
            this.totalAgents = totalAgents;
            this.activeAgents = activeAgents;
            this.totalConversations = totalConversations;
        }

        public String getUserId() {
            return userId;
        }

        public int getTotalAgents() {
            return totalAgents;
        }

        public int getActiveAgents() {
            return activeAgents;
        }

        public int getTotalConversations() {
            return totalConversations;
        }
    }
}
