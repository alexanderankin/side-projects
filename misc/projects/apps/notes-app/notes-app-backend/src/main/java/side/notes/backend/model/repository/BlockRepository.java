package side.notes.backend.model.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import side.notes.backend.model.entity.BlockEntity;
import side.notes.backend.model.entity.NoteEntity;

import java.util.Optional;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<BlockEntity, UUID> {
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "tags")
    Page<BlockEntity> findAllByNoteIsOrderByOrdinalAsc(NoteEntity note, Pageable pageable);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "tags")
    Page<BlockEntity> findAllByNoteIsAndOrdinalGreaterThanOrderByOrdinalAsc(NoteEntity note, String ordinalIsGreaterThan, Pageable pageable);

    Optional<BlockEntity> findTop1ByNote_IdOrderByOrdinalDesc(UUID noteId);

    int deleteByNote_IdAndId(UUID noteId, UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "tags")
    @NonNull
    @Override
    Optional<BlockEntity> findById(@NonNull UUID id);
}
