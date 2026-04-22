package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.Agent;

/** JPA-Repository fuer {@link Agent}-Entitaeten (CRUD-Operationen). */
public interface AgentRepository extends JpaRepository<Agent, UUID> {

}
