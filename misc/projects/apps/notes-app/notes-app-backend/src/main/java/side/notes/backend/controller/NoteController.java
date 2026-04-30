package side.notes.backend.controller;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import side.notes.backend.model.UpToOneSort;
import side.notes.backend.model.entity.*;
import side.notes.backend.model.repository.BlockRepository;
import side.notes.backend.model.repository.NoteRepository;
import side.notes.backend.model.repository.TagRepository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notes")
@Validated
@Transactional
public class NoteController {
    private final EntityManager entityManager;
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final BlockRepository blockRepository;
    private final TagRepository tagRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    NoteEntity createOne(@NotNull @Valid NoteEntity noteEntity) {
        entityManager.persist(noteEntity);
        return noteEntity;
    }

    @GetMapping
    PagedModel<NoteEntity> getAll(@UpToOneSort Pageable pageable, @RequestParam(required = false) String lastId) {
        if (lastId != null) {
            var page = switch (pageable.getSort().iterator().next().getProperty()) {
                case "created" -> noteRepository.findAllByCreatedAfter(OffsetDateTime.parse(lastId), pageable);
                case "updated" -> noteRepository.findAllByUpdatedAfter(OffsetDateTime.parse(lastId), pageable);
                case "name" -> noteRepository.findAllByNameAfter(lastId, pageable);
                // todo put this in the validation layer (@SortIs({"created", "updated", "name"}))
                default ->
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only support sorts: created, updated, name");
            };
            return new PagedModel<>(page);
        }
        return new PagedModel<>(noteRepository.findAll(pageable));
    }

    @PutMapping(path = "/{noteId}")
    NoteEntity updateOne(@NotNull @Valid NoteEntity noteEntity, @PathVariable UUID noteId) {
        NoteEntity managed = noteRepository.getReferenceById(noteId);
        noteMapper.updateNoteEntity(managed, noteEntity);
        return managed;
    }

    @GetMapping(path = "/{noteId}")
    NoteEntity getOne(@PathVariable UUID noteId) {
        return noteRepository.findById(noteId).orElse(null);
    }

    @DeleteMapping(path = "/{noteId}")
    NoteEntity deleteOne(@PathVariable UUID noteId) {
        return noteRepository.findById(noteId).stream().peek(noteRepository::delete).findAny().orElse(null);
    }

    @Validated({ReferencedEntity.class})
    @PostMapping(path = "/{noteId}/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    BlockEntity createBlock(@PathVariable UUID noteId, @NotNull @Valid BlockEntity blockEntity) {
        var note = noteRepository.findById(noteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var size = note.getSize();
        note.setSize(size + 1);

        blockEntity.setOrdinal(size);
        blockEntity.setNote(note);
        blockEntity.setTags(referencesForTags(blockEntity.getTags()));
        entityManager.persist(blockEntity);
        return blockEntity;
    }

    private SortedSet<TagEntity> referencesForTags(SortedSet<TagEntity> tags) {
        return CollectionUtils.emptyIfNull(tags).stream()
                .map(TagEntity::getId)
                .map(tagRepository::getReferenceById)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @GetMapping(path = "/{noteId}/blocks")
    PagedModel<BlockEntity> getAllBlocks(@PathVariable UUID noteId,
                                         Pageable pageable,
                                         @RequestParam(defaultValue = "0") Integer lastOrdinal) {
        return new PagedModel<>(blockRepository.findAllByNoteIsAndOrdinalGreaterThan(
                noteRepository.getReferenceById(noteId),
                lastOrdinal,
                pageable
        ));
    }

    @Validated({ReferencedEntity.class})
    @PutMapping(path = "/{noteId}/blocks/{blockId}")
    BlockEntity updateBlockEntity(@PathVariable UUID noteId, @PathVariable UUID blockId, @NotNull @Valid BlockEntity blockEntity) {
        blockEntity.setId(blockId);
        blockEntity.setNote(noteRepository.getReferenceById(noteId));
        blockEntity.setTags(referencesForTags(blockEntity.getTags()));
        return blockRepository.save(blockEntity);
    }

    @GetMapping(path = "/{noteId}/blocks/{blockId}")
    BlockEntity getBlockEntity(@PathVariable UUID noteId, @PathVariable UUID blockId) {
        return blockRepository.findById(blockId)
                .filter(b -> Objects.equals(b.getNote().getId(), noteId))
                .orElse(null);
    }

    @DeleteMapping(path = "/{noteId}/blocks/{blockId}")
    BlockEntity deleteBlockEntity(@PathVariable UUID noteId, @PathVariable UUID blockId) {
        return blockRepository.findById(blockId)
                .filter(b -> Objects.equals(b.getNote().getId(), noteId))
                .stream().peek(blockRepository::delete).findAny()
                .orElse(null);
    }
}
