package side.notes.backend.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import side.notes.backend.model.UpToOneSort;
import side.notes.backend.model.entity.*;
import side.notes.backend.model.mapper.NoteMapper;
import side.notes.backend.model.repository.BlockRepository;
import side.notes.backend.model.repository.NoteRepository;
import side.notes.backend.model.repository.TagRepository;
import side.notes.backend.service.FractionalIndexService;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
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
    private final FractionalIndexService fractionalIndexService;

    @JsonView(Views.Default.class)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    NoteEntity createOne(@NotNull @Valid @RequestBody NoteEntity noteEntity) {
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

    @JsonView(Views.Default.class)
    @PutMapping(path = "/{noteId}")
    NoteEntity updateOne(@NotNull @Valid @RequestBody NoteEntity noteEntity, @PathVariable UUID noteId) {
        NoteEntity managed = noteRepository.getReferenceById(noteId);
        noteMapper.updateNoteEntity(managed, noteEntity);
        return managed;
    }

    @JsonView(Views.Default.class)
    @GetMapping(path = "/{noteId}")
    NoteEntity getOne(@PathVariable UUID noteId) {
        return noteRepository.findById(noteId).orElse(null);
    }

    @JsonView(Views.Default.class)
    @DeleteMapping(path = "/{noteId}")
    NoteEntity deleteOne(@PathVariable UUID noteId) {
        return noteRepository.findById(noteId).stream().peek(noteRepository::delete).findAny().orElse(null);
    }

    @JsonView(Views.Default.class)
    @PostMapping(path = "/{noteId}/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    BlockEntity createBlock(@PathVariable UUID noteId, @NotNull @Valid @RequestBody BlockEntity blockEntity) {
        var lastBlock = blockRepository.findTop1ByNote_IdOrderByOrdinalDesc(noteId);
        var lastOrdinal = lastBlock.map(BlockEntity::getOrdinal).orElse(null);
        var nextOrdinal = fractionalIndexService.generateKeyBetween(lastOrdinal, null);
        blockEntity.setOrdinal(nextOrdinal);
        blockEntity.setNote(noteRepository.getReferenceById(noteId));
        blockEntity.setTags(referencesForTags(blockEntity.getTags()));
        entityManager.persist(blockEntity);
        return blockEntity;
    }

    @JsonView(Views.Default.class)
    @PostMapping(path = "/{noteId}/blocks", params = {"afterOrdinal"})
    @ResponseStatus(HttpStatus.CREATED)
    @NotNull
    @Valid
    BlockEntity createBlock(@PathVariable UUID noteId,
                            @NotEmpty @RequestParam String afterOrdinal,
                            @NotEmpty @RequestParam String beforeOrdinal,
                            @NotNull @Valid @RequestBody BlockEntity blockEntity) {
        var nextOrdinal = fractionalIndexService.generateKeyBetween(afterOrdinal, beforeOrdinal);
        blockEntity.setOrdinal(nextOrdinal);
        blockEntity.setNote(noteRepository.getReferenceById(noteId));
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

    @JsonView(Views.Default.Partial.class)
    @GetMapping(path = "/{noteId}/blocks")
    PagedModel<BlockEntity> getAllBlocks(@PathVariable UUID noteId,
                                         Pageable pageable,
                                         @RequestParam(defaultValue = "") String lastOrdinal) {
        var note = noteRepository.getReferenceById(noteId);
        Page<BlockEntity> result = !StringUtils.hasText(lastOrdinal)
                ? blockRepository.findAllByNoteIsOrderByOrdinalAsc(note, pageable)
                : blockRepository.findAllByNoteIsAndOrdinalGreaterThanOrderByOrdinalAsc(note, lastOrdinal, pageable);
        return new PagedModel<>(result);
    }

    @PutMapping(path = "/{noteId}/blocks/{blockId}")
    BlockEntity updateBlockEntity(@PathVariable UUID noteId, @PathVariable UUID blockId, @NotNull @Valid @RequestBody BlockEntity blockEntity) {
        blockEntity.setId(blockId);
        blockEntity.setNote(noteRepository.getReferenceById(noteId));
        blockEntity.setTags(referencesForTags(blockEntity.getTags()));
        return blockRepository.save(blockEntity);
    }

    @JsonView(Views.Default.class)
    @GetMapping(path = "/{noteId}/blocks/{blockId}")
    BlockEntity getBlockEntity(@PathVariable UUID noteId, @PathVariable UUID blockId) {
        return blockRepository.findById(blockId)
                .filter(b -> Objects.equals(b.getNote().getId(), noteId))
                .orElse(null);
    }

    @JsonView(Views.Default.class)
    @DeleteMapping(path = "/{noteId}/blocks/{blockId}", params = "returning")
    BlockEntity deleteBlockEntity(@PathVariable UUID noteId, @PathVariable UUID blockId, @RequestParam boolean returning) {
        return blockRepository.findById(blockId)
                .filter(b -> Objects.equals(b.getNote().getId(), noteId))
                .stream().peek(blockRepository::delete).findAny()
                .orElse(null);
    }

    @DeleteMapping(path = "/{noteId}/blocks/{blockId}")
    @ResponseStatus(HttpStatus.OK)
    void deleteBlockEntity(@PathVariable UUID noteId, @PathVariable UUID blockId) {
        var deleted = blockRepository.deleteByNote_IdAndId(noteId, blockId);
        if (deleted == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
