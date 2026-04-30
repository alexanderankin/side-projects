package side.notes.backend.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import side.notes.backend.model.entity.BlockEntity;
import side.notes.backend.model.entity.NoteEntity;

import java.util.UUID;

public interface BlockRepository extends JpaRepository<BlockEntity, UUID> {
    Page<BlockEntity> findAllByNoteIsAndOrdinalGreaterThan(NoteEntity note, Integer ordinalIsGreaterThan, Pageable pageable);
}
