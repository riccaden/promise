package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.Utterance;

/** JPA-Repository fuer {@link Utterance}-Entitaeten (einzelne Nachrichten). */
public interface UtteranceRepository extends JpaRepository<Utterance, UUID> {

}
