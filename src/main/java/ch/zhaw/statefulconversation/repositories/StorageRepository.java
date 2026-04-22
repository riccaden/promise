package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.Storage;

/** JPA-Repository fuer {@link Storage}-Entitaeten (CRUD-Operationen). */
public interface StorageRepository extends JpaRepository<Storage, UUID> {

}
