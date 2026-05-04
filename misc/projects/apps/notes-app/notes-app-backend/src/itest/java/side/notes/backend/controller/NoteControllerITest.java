package side.notes.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import side.notes.backend.NotesBackendITest;
import side.notes.backend.model.PagedModelDto;
import side.notes.backend.model.entity.BlockEntity;
import side.notes.backend.model.entity.NoteEntity;
import side.notes.backend.model.entity.TagEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NoteControllerITest extends NotesBackendITest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createNote() {
        var prefix = "createNote";
        var result = webTestClient.post().uri("/api/notes").bodyValue(new NoteEntity().setName(prefix)).exchange().expectStatus().isCreated().expectBody(NoteEntity.class).value(notNullValue()).returnResult();
        assertThat(result.getResponseBody(), is(notNullValue()));
        assertThat(result.getResponseBody().getId(), is(notNullValue()));
        assertThat(result.getResponseBody().getCreated(), is(notNullValue()));
        assertThat(result.getResponseBody().getUpdated(), is(notNullValue()));
        assertThat(result.getResponseBody().getName(), is(prefix));
        assertThat(result.getResponseBody().getDescription(), is(nullValue()));
        System.out.println(result.getResponseBody());
    }

    @Test
    void typicalUsage() {
        var prefix = "typicalUsage";
        var noteId = webTestClient.post().uri("/api/notes")
                .bodyValue(new NoteEntity().setName(prefix))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(NoteEntity.class)
                .returnResult().getResponseBody().getId();

        List<BlockEntity> initialBlocks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            initialBlocks.add(webTestClient.post()
                    .uri("/api/notes/{noteId}/blocks", noteId)
                    .bodyValue(new BlockEntity().setContent("block: " + (i + 1)))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(BlockEntity.class)
                    .returnResult().getResponseBody());
        }

        webTestClient.post()
                .uri(
                        "/api/notes/{noteId}/blocks?afterOrdinal={b0}&beforeOrdinal={b1}",
                        noteId,
                        initialBlocks.getFirst().getOrdinal(),
                        initialBlocks.get(1).getOrdinal()
                )
                .bodyValue(new BlockEntity().setContent("second"))
                .exchange()
                .expectStatus().isCreated();

        var blocks = webTestClient.get().uri("/api/notes/{noteId}/blocks", noteId).exchange().expectBody(new ParameterizedTypeReference<PagedModelDto<BlockEntity>>() {
        }).returnResult().getResponseBody();
        assertThat(blocks, is(notNullValue()));
        assertThat(blocks.getPage().size(), is(20L));
        assertThat(blocks.getPage().number(), is(0L));
        assertThat(blocks.getPage().totalElements(), is(4L));
        assertThat(blocks.getPage().totalPages(), is(1L));
        assertThat(blocks.getContent(), hasSize(4));
        assertThat(blocks.getContent().getFirst().getContent(), is(notNullValue()));
        assertThat(blocks.getContent(), contains(
                hasProperty("content", is("block: 1")),
                hasProperty("content", is("second")),
                hasProperty("content", is("block: 2")),
                hasProperty("content", is("block: 3"))
        ));
        assertThat(blocks.getContent(), everyItem(hasProperty("tags", is(notNullValue()))));
        assertThat(blocks.getContent(), everyItem(hasProperty("tags", is(empty()))));
    }

    @Test
    void getBlocksSupportsOrdinalCursor() {
        var prefix = "getBlocksSupportsOrdinalCursor-";
        var noteId = webTestClient.post().uri("/api/notes")
                .bodyValue(new NoteEntity().setName(prefix + "-note"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(NoteEntity.class)
                .returnResult()
                .getResponseBody()
                .getId();
        var firstBlock = webTestClient.post()
                .uri("/api/notes/{noteId}/blocks", noteId)
                .bodyValue(new BlockEntity().setContent(prefix + "-1"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BlockEntity.class)
                .returnResult()
                .getResponseBody();
        var secondBlock = webTestClient.post()
                .uri("/api/notes/{noteId}/blocks", noteId)
                .bodyValue(new BlockEntity().setContent(prefix + "-2"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BlockEntity.class)
                .returnResult()
                .getResponseBody();
        webTestClient.post()
                .uri("/api/notes/{noteId}/blocks", noteId)
                .bodyValue(new BlockEntity().setContent(prefix + "-3"))
                .exchange()
                .expectStatus().isCreated();

        var firstPage = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notes/{noteId}/blocks")
                        .queryParam("size", 2)
                        .build(noteId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedModelDto<BlockEntity>>() {
                })
                .returnResult()
                .getResponseBody();
        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getContent(), hasSize(2));
        assertThat(firstPage.getContent(), contains(
                hasProperty("content", is(prefix + "-1")),
                hasProperty("content", is(prefix + "-2"))
        ));

        var secondPage = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notes/{noteId}/blocks")
                        .queryParam("size", 2)
                        .queryParam("lastOrdinal", secondBlock.getOrdinal())
                        .build(noteId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedModelDto<BlockEntity>>() {
                })
                .returnResult()
                .getResponseBody();
        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getContent(), hasSize(1));
        assertThat(secondPage.getContent(), contains(hasProperty("content", is(prefix + "-3"))));
        assertThat(firstBlock.getOrdinal(), is(notNullValue()));
        assertThat(secondBlock.getOrdinal(), is(notNullValue()));
    }

    @Test
    void putBlockAddsAndRemovesTags() {
        var prefix = "putBlockAddsAndRemovesTags-";
        var noteId = webTestClient.post().uri("/api/notes")
                .bodyValue(new NoteEntity().setName(prefix + "note"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(NoteEntity.class)
                .returnResult()
                .getResponseBody()
                .getId();
        var createdBlock = webTestClient.post()
                .uri("/api/notes/{noteId}/blocks", noteId)
                .bodyValue(new BlockEntity().setContent(prefix + "block"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BlockEntity.class)
                .returnResult()
                .getResponseBody();
        assertThat(createdBlock, is(notNullValue()));
        var tagToCreate = (TagEntity) new TagEntity()
                .setDescription(prefix + "description")
                .setName(prefix + "tag");
        var createdTag = webTestClient.post().uri("/api/tags")
                .bodyValue(tagToCreate)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TagEntity.class)
                .returnResult()
                .getResponseBody();
        assertThat(createdTag, is(notNullValue()));

        var updateWithTag = objectMapper.convertValue(createdBlock, BlockEntity.class);
        updateWithTag.setTags(new TreeSet<>(List.of((TagEntity) new TagEntity().setName(createdTag.getName()).setId(createdTag.getId()))));

        var updatedWithTag = webTestClient.put()
                .uri("/api/notes/{noteId}/blocks/{blockId}", noteId, createdBlock.getId())
                .bodyValue(updateWithTag)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BlockEntity.class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedWithTag, is(notNullValue()));
        assertThat(updatedWithTag.getId(), is(createdBlock.getId()));
        assertThat(updatedWithTag.getContent(), is(createdBlock.getContent()));
        assertThat(updatedWithTag.getTags(), hasSize(1));
        assertThat(updatedWithTag.getTags(), contains(hasProperty("id", is(createdTag.getId()))));
        assertThat(updatedWithTag.getTags(), contains(hasProperty("name", is(createdTag.getName()))));

        var persistedWithTag = webTestClient.get()
                .uri("/api/notes/{noteId}/blocks/{blockId}", noteId, createdBlock.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BlockEntity.class)
                .returnResult()
                .getResponseBody();
        assertThat(persistedWithTag, is(notNullValue()));
        assertThat(persistedWithTag.getTags(), hasSize(1));
        assertThat(persistedWithTag.getTags(), contains(hasProperty("id", is(createdTag.getId()))));

        var updateWithoutTags = new BlockEntity();
        updateWithoutTags.setOrdinal(createdBlock.getOrdinal());
        updateWithoutTags.setContent(prefix + "block cleared");
        updateWithoutTags.setTags(new TreeSet<>());

        var updatedWithoutTags = webTestClient.put()
                .uri("/api/notes/{noteId}/blocks/{blockId}", noteId, createdBlock.getId())
                .bodyValue(updateWithoutTags)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BlockEntity.class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedWithoutTags, is(notNullValue()));
        assertThat(updatedWithoutTags.getContent(), is(prefix + "block cleared"));
        assertThat(updatedWithoutTags.getTags(), is(empty()));

        var persistedWithoutTags = webTestClient.get()
                .uri("/api/notes/{noteId}/blocks/{blockId}", noteId, createdBlock.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BlockEntity.class)
                .returnResult()
                .getResponseBody();
        assertThat(persistedWithoutTags, is(notNullValue()));
        assertThat(persistedWithoutTags.getTags(), is(empty()));
    }

    @Test
    void getNotesSupportsNameCursorAndRejectsUnsupportedSort() {
        var prefix = "getNotesSupportsNameCursor-";
        var firstName = prefix + "-a";
        var secondName = prefix + "-b";

        webTestClient.post().uri("/api/notes")
                .bodyValue(new NoteEntity().setName(firstName))
                .exchange()
                .expectStatus().isCreated();
        webTestClient.post().uri("/api/notes")
                .bodyValue(new NoteEntity().setName(secondName))
                .exchange()
                .expectStatus().isCreated();

        var page = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notes")
                        .queryParam("sort", "name,asc")
                        .queryParam("size", 1)
                        .queryParam("lastId", firstName)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedModelDto<NoteEntity>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(page, is(notNullValue()));
        assertThat(page.getContent(), hasSize(1));
        assertThat(page.getContent(), contains(hasProperty("name", is(secondName))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notes")
                        .queryParam("sort", "description,asc")
                        .queryParam("lastId", firstName)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getTagsSupportsDescriptionCursorAndRejectsUnsupportedSort() {
        var prefix = "getTagsSupportsDescriptionCursor-";
        var firstDescription = prefix + "-a";
        var secondDescription = prefix + "-b";
        var firstTag = new TagEntity();
        firstTag.setName(prefix + "-tag-a");
        firstTag.setDescription(firstDescription);
        var secondTag = new TagEntity();
        secondTag.setName(prefix + "-tag-b");
        secondTag.setDescription(secondDescription);

        webTestClient.post().uri("/api/tags")
                .bodyValue(firstTag)
                .exchange()
                .expectStatus().isCreated();
        webTestClient.post().uri("/api/tags")
                .bodyValue(secondTag)
                .exchange()
                .expectStatus().isCreated();

        var page = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/tags")
                        .queryParam("sort", "description,asc")
                        .queryParam("size", 1)
                        .queryParam("lastId", firstDescription)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedModelDto<TagEntity>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(page, is(notNullValue()));
        assertThat(page.getContent(), hasSize(1));
        assertThat(page.getContent(), contains(hasProperty("description", is(secondDescription))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/tags")
                        .queryParam("sort", "id,asc")
                        .queryParam("lastId", firstDescription)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }
}
