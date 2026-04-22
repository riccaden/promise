package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.State;

/** JPA-Repository fuer {@link State}-Entitaeten (CRUD-Operationen). */
public interface StateRepository extends JpaRepository<State, UUID> {

}
