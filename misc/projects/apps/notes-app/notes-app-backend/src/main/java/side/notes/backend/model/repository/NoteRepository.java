package side.notes.backend.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import side.notes.backend.model.entity.NoteEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
    Page<NoteEntity> findAllByCreatedAfter(OffsetDateTime nameAfter, Pageable pageable);

    Page<NoteEntity> findAllByUpdatedAfter(OffsetDateTime nameAfter, Pageable pageable);

    Page<NoteEntity> findAllByNameAfter(String nameAfter, Pageable pageable);

    // doesn't work because we do actually need the result
    // @Modifying
    // @Query("update NoteEntity e set e.size = e.size + 1 where e.id = :noteId")
    // void incrementSizeOnNoteEntity(@Param("noteId") UUID noteId);
}
