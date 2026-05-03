package side.notes.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import side.notes.backend.NotesBackendITest;
import side.notes.backend.model.PagedModelDto;
import side.notes.backend.model.entity.BlockEntity;
import side.notes.backend.model.entity.NoteEntity;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NoteControllerITest extends NotesBackendITest {
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
}
