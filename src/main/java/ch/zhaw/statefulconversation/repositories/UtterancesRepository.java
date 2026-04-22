package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.Utterances;

/** JPA-Repository fuer {@link Utterances}-Entitaeten (Nachrichtensammlungen/Gespraechsverlaeufe). */
public interface UtterancesRepository extends JpaRepository<Utterances, UUID> {

}
