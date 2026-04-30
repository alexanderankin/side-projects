package side.notes.backend.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import side.notes.backend.model.entity.TagEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {
    Page<TagEntity> findAllByCreatedAfter(OffsetDateTime created, Pageable pageable);

    Page<TagEntity> findAllByUpdatedAfter(OffsetDateTime updated, Pageable pageable);

    Page<TagEntity> findAllByNameAfter(String name, Pageable pageable);

    Page<TagEntity> findAllByDescriptionAfter(String description, Pageable pageable);
}
