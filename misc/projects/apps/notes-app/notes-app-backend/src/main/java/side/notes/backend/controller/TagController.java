package side.notes.backend.controller;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import side.notes.backend.model.entity.TagEntity;
import side.notes.backend.model.mapper.TagMapper;
import side.notes.backend.model.repository.TagRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tags")
@Validated
@Transactional
public class TagController {
    private final EntityManager entityManager;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TagEntity create(@NotNull @Valid @RequestBody TagEntity tagEntity) {
        entityManager.persist(tagEntity);
        return tagEntity;
    }

    @GetMapping
    PagedModel<TagEntity> getAll(Pageable pageable, @RequestParam(required = false) String lastId) {
        if (lastId != null) {
            var page = switch (pageable.getSort().iterator().next().getProperty()) {
                case "created" -> tagRepository.findAllByCreatedAfter(OffsetDateTime.parse(lastId), pageable);
                case "updated" -> tagRepository.findAllByUpdatedAfter(OffsetDateTime.parse(lastId), pageable);
                case "name" -> tagRepository.findAllByNameAfter(lastId, pageable);
                case "description" -> tagRepository.findAllByDescriptionAfter(lastId, pageable);
                // todo put this in the validation layer (@SortIs({"created", "updated", "name"}))
                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "only support sorts: created, updated, name, description");
            };
            return new PagedModel<>(page);
        }
        return new PagedModel<>(tagRepository.findAll(pageable));
    }

    @PutMapping(path = "/{tagId}")
    TagEntity putTagEntity(@PathVariable UUID tagId, @NotNull @Valid @RequestBody TagEntity tagEntity) {
        var managed = tagRepository.getReferenceById(tagId);
        tagMapper.updateTagEntity(managed, tagEntity);
        return tagRepository.save(tagEntity);
    }
}
