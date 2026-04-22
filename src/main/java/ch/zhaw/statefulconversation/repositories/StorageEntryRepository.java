package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.StorageEntry;

/** JPA-Repository fuer {@link StorageEntry}-Entitaeten (CRUD-Operationen). */
public interface StorageEntryRepository extends JpaRepository<StorageEntry, UUID> {

}
